package com.nem

import org.specs2.Specification
import util.TypeSafeConfig
import it.sauronsoftware.ftp4j.FTPClient

class Ftp4JDownloadSpec() extends Specification with IFtp4jSpec {
  val fileName = "../../ftp4jExample.conf"
  val address = TypeSafeConfig.settings[String](fileName, "address").get
  val password = TypeSafeConfig.settings[String](fileName, "password").get
  val username = TypeSafeConfig.settings[String](fileName, "username").get
  val directory = TypeSafeConfig.settings[String](fileName, "directory").get
  val file = TypeSafeConfig.settings[String](fileName, "file").get
  val port = TypeSafeConfig.settings[Int](fileName, "port").get

  val runner: FTPClientRunner = FTPClientRunner(address, port, file, directory,
    username, password, "none",5)

  override def is =
    args(sequential = true) ^
      "This spec verifies the 'Ftp4J' libraries download functionality." ^
      p ^
      "'Ftp4JDownloadSpec' should " ^
      "Successful connection to an FTP server" ! test1_connected ^
      "Successful authentication to an FTP server" ! test2_authenticated ^
      "Successful download of a image from a FTP server" ! test3_download ^
      end

  def test1_connected = {
    val client = runner.startOrRefreshClient()
    client.isConnected
  }

  def test2_authenticated = {
    val client = runner.startOrRefreshClient()
    client.isAuthenticated
  }

  def test3_download = {
    runner.startOrRefreshClient()
    val image = runner.retrieveImage()
    if (image.array() != null)
    {
      com.nem.util.InputOutput.writeByteBufferToNewFile("imageTest.jpg",image)
      image.array().length > 0
    }
    else false
  }
}
