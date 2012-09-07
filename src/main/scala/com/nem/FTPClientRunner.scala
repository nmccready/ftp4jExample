package com.nem

import it.sauronsoftware.ftp4j._
import it.sauronsoftware.ftp4j.connectors._
import scala.io.Source._
import io.{Codec, BufferedSource}
import java.nio.ByteBuffer
import util.ILogger
import org.slf4j.LoggerFactory

case class FTPClientRunner(addressWithOutFileName: String,
                           port: Int,
                           imageFileName: String,
                           directory: String,
                           user: String,
                           password: String,
                           proxyType: String,
                           client: FTPClient) extends App with ILogger {

  protected val slf4jLogger = createLogger(getClass)

  val maxAttempts = 5
  var isHealthy = true

  def startOrRefreshClient() {
    var currentConnAttempts = 0
    var connect:() => Unit = null
    ConnectionProxyType.createConnectorFromString(proxyType, addressWithOutFileName, port,
      user, password) match {
      case Some(proxy) =>
        connect = () => client.setConnector(proxy)
      case None =>
        connect = () => {
          client.connect(addressWithOutFileName, port)
          client.login(user,password)
        }
    }
    try {
      if (client.isConnected)
        return
      while (currentConnAttempts <= maxAttempts && !client.isConnected) {
        currentConnAttempts += 1
        connect.apply()
      }
      if (client.isConnected)
        isHealthy = true
      if(!client.isConnected) {
        isHealthy = false
        throw new FtpJpegSourceException("Max connection attempts have been exceeded!")
      }
    }
    catch {
      case e: Exception =>
        val exception = e
        error("FTP connection failure at addressWithOutFileName(%s) and port(%s)".
          format(addressWithOutFileName, port.toString),
          exception)
    }
  }

  protected def forceConnectionStop() {
    //set time on disconnect, if time out call disconnect false (exit ungraceful)
    client.disconnect(true)
    debug("ftp connection force stopped.")
  }

  def retrieveImage(): ByteBuffer = {
    if (!client.isConnected)
      startOrRefreshClient()
    if (!client.isConnected || !client.isAuthenticated)
      return null

    var optSource: Option[BufferedSource] = None
    try {
      val downloadFile = new java.io.File(imageFileName)
      client.changeDirectory(directory)
      client.download(imageFileName, downloadFile)
      optSource = Some(fromFile(downloadFile,"latin1"))
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
        error("Exception! Error downloading image. ", ex)
        Console.println("Exception! Error downloading image. " +  ex.getMessage)
        null
    }
    finally {
      optSource match {
        case Some(source) => source.close()
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

object ConnectionProxyType extends ILogger {
  protected val slf4jLogger = createLogger(getClass)

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
        error("Uknown Connection Proxy Type " + unknown)
        None
    }
  }
}
