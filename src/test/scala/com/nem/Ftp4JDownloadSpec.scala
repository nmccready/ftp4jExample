package com.nem

import org.specs2.Specification
import it.sauronsoftware.ftp4j._
import it.sauronsoftware.ftp4j.connectors._
import scala.io.Source._
import io.BufferedSource

class Ftp4JDownloadSpec extends Specification with IFtp4jSpec {
  val client:FTPClient
  def is =
      args(sequential = true) ^
      "This spec verifies the 'Ftp4J' libraries download functionality." ^
      p ^
      "'Ftp4JDownloadSpec' should " ^
      "Successful connection to an FTP server" ! test1 ^
      "Successful download of a image from a FTP server" ! test1 ^
      end

  def test1 = {
    false
  }
}
