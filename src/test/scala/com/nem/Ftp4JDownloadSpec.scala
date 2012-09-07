package com.nem

import org.specs2.Specification
import util.TypeSafeConfig
import it.sauronsoftware.ftp4j.FTPClient

class Ftp4JDownloadSpec() extends Specification with IFtp4jSpec {
  val fileName = "../../ftp4jExample.conf"
  val address = TypeSafeConfig.settings[String](fileName,"address").get
  val password = TypeSafeConfig.settings[String](fileName,"password").get
  val username = TypeSafeConfig.settings[String](fileName,"username").get
  val directory = TypeSafeConfig.settings[String](fileName,"directory").get
  val file = TypeSafeConfig.settings[String](fileName,"file").get
  val port = TypeSafeConfig.settings[Int](fileName,"port").get

  val client:FTPClientRunner = FTPClientRunner(address,port,file,directory,
    username,password,"none",new FTPClient())

  override def is =
      args(sequential = true) ^
      "This spec verifies the 'Ftp4J' libraries download functionality." ^
      p ^
      "'Ftp4JDownloadSpec' should " ^
      "Successful connection to an FTP server" ! test1 ^
      "Successful download of a image from a FTP server" ! test2 ^
      end

  def test1 = {
    false
  }
  def test2 = {
    false
  }
}
