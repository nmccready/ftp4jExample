package com.nem

import it.sauronsoftware.ftp4j._
import it.sauronsoftware.ftp4j.connectors._
import scala.io.Source._
import io.BufferedSource
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory

case class FTPClientRunner(addressWithOutFileName: String,
                           port: Int,
                           imageFileName: String,
                           directory: String,
                           user: String,
                           password: String,
                           proxyType: String,
                           maxAttempts: Int,
                           connectSleepMilli: Int = 1000) {

  protected val logger = LoggerFactory.getLogger(getClass)
  @volatile private var doConnect = false

  def startOrRefreshClient():FTPClient = {
    doConnect = true
    val client:FTPClient = new FTPClient()
    var currentConnAttempts = 0
    var connect: () => Unit = null
    ConnectionProxyType.createConnectorFromString(proxyType, addressWithOutFileName, port,
      user, password) match {
      case Some(proxy) =>
        connect = () => client.setConnector(proxy)
      case None =>
        connect = () => {
          client.connect(addressWithOutFileName, port)
          client.login(user, password)
        }
    }
    try {
      if (client.isConnected){
        doConnect = false
        return client
      }
      while (currentConnAttempts <= maxAttempts && !client.isConnected && doConnect) {
        currentConnAttempts += 1
        try {
          connect.apply()
          doConnect = false
        } catch {
          case e: Exception =>
        }
        Thread.sleep(connectSleepMilli)
      }
      if (!client.isConnected){
        doConnect = false
        throw new FtpJpegSourceException("Max connection attempts have been exceeded!")
      }
    }
    catch {
      case e: Exception =>
        val exception = e
        logger.error("FTP connection failure at addressWithOutFileName(%s) and port(%s)".
          format(addressWithOutFileName, port.toString),
          exception)
    }
    client
  }

  protected def forceConnectionStop(client:FTPClient) {
    //set time on disconnect, if time out call disconnect false (exit ungraceful)
    client.disconnect(true)
    logger.debug("ftp connection force stopped.")
  }

  def retrieveImage(): ByteBuffer = {
      val client = startOrRefreshClient()
    if (!client.isConnected || !client.isAuthenticated)
      return null

    var optSource: Option[BufferedSource] = None
    var downloadFile: Option[java.io.File] = None
    try {
      downloadFile = Some(new java.io.File(imageFileName))
      if (directory != "" && directory != null)
        client.changeDirectory(directory)
      client.download(imageFileName, downloadFile.get)
      optSource = Some(fromFile(downloadFile.get, "latin1"))
      optSource match {
        case Some(source) =>
          val fileAsBytes = source.map(_.toByte).toArray
          ByteBuffer.wrap(fileAsBytes)
        case None =>
          null
      }
    }
    catch {
      case e: Exception =>
        val ex = e
        logger.error("Exception! Error downloading image. ", ex)
        Console.println("Exception! Error downloading image. " + ex.getMessage)
        null
    }
    finally {
      if (client.isConnected)
        this.forceConnectionStop(client)
      optSource match {
        case Some(source) => source.close()
        case None => null
      }
      downloadFile match {
        case Some(file) => file.delete()
        case None => null
      }
    }
  }
}

case class FtpJpegSourceException(message: String, exception: Option[Exception]) extends Exception(message) {
  def this(exception: Exception) = this("FtpJpegSourceException: " + exception.getMessage, Some(exception))

  def this(message: String) = this("FtpJpegSourceException: " + message, None)

  if (exception.isDefined)
    super.setStackTrace(exception.get.getStackTrace)
}

object ConnectionProxyType {
  protected val logger = LoggerFactory.getLogger(getClass)

  final val none = "none"
  final val http = "http"
  final val ftp = "ftp"
  final val socks4 = "socks4"
  final val socks5 = "socks5"

  def createConnectorFromString(connectorTypeAsString: String, address: String,
                                port: Int, userName: String, password: String): Option[FTPConnector] = {
    connectorTypeAsString match {
      case ConnectionProxyType.http => Some(new HTTPTunnelConnector(address, port, userName, password))
      case ConnectionProxyType.ftp => Some(new FTPProxyConnector(address, port, userName, password))
      case ConnectionProxyType.socks4 => Some(new SOCKS4Connector(address, port, userName))
      case ConnectionProxyType.socks5 => Some(new SOCKS5Connector(address, port, userName, password))
      case ConnectionProxyType.none => None
      case unknown =>
        logger.error("Uknown Connection Proxy Type " + unknown)
        None
    }
  }
}

