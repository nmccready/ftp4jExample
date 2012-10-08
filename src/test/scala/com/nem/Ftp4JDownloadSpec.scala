package com.nem

import org.specs2.Specification
import util.TypeSafeConfig
import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import java.nio.ByteBuffer

class Ftp4JDownloadSpec() extends Specification with IFtp4jSpec {
  val fileName = "../../ftp4jExample.conf"
  val test1Obj = TypeSafeConfig.settings[java.util.HashMap[String, String]](fileName, "test1").get

  val runner = parseHashMapToFTPClientRunner(test1Obj)

  override def is =
    args(sequential = true) ^
      "This spec verifies the 'Ftp4J' libraries download functionality." ^
      p ^
      "'Ftp4JDownloadSpec' should " ^
      "Successful connection to an FTP server" ! test1_connected ^
      "Successful authentication to an FTP server" ! test2_authenticated ^
      "Successful download of a image from a FTP server" ! test3_download ^
      "Successful test of multiple concurrent cameras" ! simulateSeveralCameras ^
      end

  def test1_connected = {
    val client = runner.startAndGetClient()
    client.isConnected
  }

  def test2_authenticated = {
    val client = runner.startAndGetClient()
    client.isAuthenticated
  }

  def test3_download = {
    runner.startAndGetClient()
    writeImage("imageOutTest3.jpg", runner.retrieveImage())
  }

  def simulateSeveralCameras() = {
    val system = ActorSystem("TestFTPRunnerSystem")
    val asyncWriter = system.actorOf(Props(new FTPClientActor), name = "ftpAsyncWriter")
    asyncWriter ! FTPClientRunnerHolder(runner, "imageOutTestAsync1.jpg")
    (2 to 10).foreach(int => {
      asyncWriter ! FTPClientRunnerHolder(parseHashMapToFTPClientRunner(
        TypeSafeConfig.settings[java.util.HashMap[String, String]](fileName, "test" + int.toString).get),
        "imageOutTestAsync" + int.toString + ".jpg")
    })
    true
  }

  def writeImage(fileNameOut: String, image: ByteBuffer): Boolean = {
    var success = false
    try {
      success = if (image.array() != null) {
        com.nem.util.InputOutput.writeByteBufferToNewFile(fileNameOut, image)
        image.array().length > 0
      }
      else false
    }
    catch {
      case e: Exception => success = false
    }
    success
  }

  def parseHashMapToFTPClientRunner(map: java.util.HashMap[String, String]): FTPClientRunner = {
    val address = map.get("address")
    val password = map.get("password")
    val username = map.get("username")
    val directory = map.get("directory")
    val file = map.get("file")
    val port = map.get("port")
    FTPClientRunner(address, port.toInt, file, directory,
      username, password, "none", 5)
  }

  case class FTPClientRunnerHolder(runner: FTPClientRunner, fileNameOut: String)

  class FTPClientActor extends Actor {
    def receive = {
      case FTPClientRunnerHolder(runner, fileNameOut) =>
        runner.startAndGetClient()
        writeImage(fileNameOut, runner.retrieveImage())
    }
  }

}
