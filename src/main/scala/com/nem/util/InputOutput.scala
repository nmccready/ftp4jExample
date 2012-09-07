package com.nem.util

import java.nio.ByteBuffer
import java.io.{FileOutputStream, File}
import java.nio.channels.FileChannel

object InputOutput {

  def writeByteBufferToNewFile(fileName: String, buffer: ByteBuffer) =
    writeByteBufferToFile(fileName, buffer, true)

  /*Set to true if the bytes should be appended to the file;
set to false if the bytes should replace current bytes
(if the file exists) */
  def writeByteBufferToFile(fileName: String, buffer: ByteBuffer, append: Boolean) = {
    val file = new File(fileName)

    var channel: FileChannel = null
    try {
      // Create a writable file channel
      channel = new FileOutputStream(file, append).getChannel()
      channel.write(buffer)
    }
    catch {
      case e: Exception =>
        throw e
    }
    finally {
      if (channel != null)
        channel.close()

    }
  }
}
