package com.nem

import it.sauronsoftware.ftp4j._
import it.sauronsoftware.ftp4j.connectors._
import scala.io.Source._
import io.BufferedSource
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

case class FTPClientRunner(addressWithOutFileName: String,
                           port: Int,
                           imageFileName: String,
                           directory: String,
                           user: String,
                           password: String,
                           proxyType: String,
                           maxAttempts: Int,
                           mode: String = "passive",
                           noopTimeout: Int = -1,
                           retryDownloadMaxCount: Int = 1,
                           connectSleepMilli: Int = 1000
                            ) {

  protected val logger = LoggerFactory.getLogger(getClass)
  @volatile private var doConnect = false

  def startAndGetClient(): FTPClient = {
    doConnect = true
    val client: FTPClient = new FTPClient()
    if (mode == "active")
      client.setPassive(false)
    client.setAutoNoopTimeout(noopTimeout)
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
      if (client.isConnected) {
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
      if (!client.isConnected) {
        doConnect = false
        throw new FtpJpegClientRunnerException("Max connection attempts have been exceeded!")
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

  protected def forceDisconnect(client: FTPClient) {
    //set time on disconnect, if time out call disconnect false (exit ungraceful)
    client.disconnect(false)
    logger.debug("ftp connection force stopped.")
  }

  protected def askServerForDisconnect(client: FTPClient) {
    try {
      client.disconnect(true)
    }
    catch {
      case exception: Exception =>
        logger.debug("ftp connection failed to stop gracefully. Now forcefully stopping connection.")
        forceDisconnect(client)
    }
  }

  def retrieveImage(): ByteBuffer = retrieveImage(0)

  def retrieveImage(attempt: Int): ByteBuffer = {
    if (attempt >= retryDownloadMaxCount)
      return null
    else if (attempt > 0)
      logger.debug("Retrieve image attempt failed, retrying...")

    val client = startAndGetClient()
    if (!client.isConnected || !client.isAuthenticated)
      return null
    var byteBuffer: ByteBuffer = null
    val outputStream = new ByteArrayOutputStream()
    byteBuffer = try {
      if (directory != "" && directory != null)
        client.changeDirectory(directory)
      client.setType(FTPClient.TYPE_BINARY)
      client.download(imageFileName, outputStream, 0, null)
      ByteBuffer.wrap(outputStream.toByteArray)
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
        askServerForDisconnect(client)
      outputStream.close()
      true
    }
    if (byteBuffer == null) {
      byteBuffer = retrieveImage(attempt + 1)
    }
    byteBuffer
  }
}

case class FtpJpegClientRunnerException(message: String, exception: Option[Exception]) extends Exception(message) {
  def this(exception: Exception) = this("FtpJpegClientRunnerException: " + exception.getMessage, Some(exception))

  def this(message: String) = this("FtpJpegClientRunnerException: " + message, None)

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