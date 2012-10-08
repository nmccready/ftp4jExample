package com.nem.util

import com.typesafe.config.ConfigFactory
import java.io.{FileNotFoundException, File}

trait ITypeSafeConfigSettings extends ILogger {

  def settings[R:Manifest](methodFileName: String, system: String): Option[R] = {
    try {
      val config = ConfigFactory.parseFile(new File(methodFileName))
      val resolved = config.resolve
      val someObj = resolved.getAnyRef(system)
      TypeHelper.cast[R](someObj)
    }
    catch {
      case fe: FileNotFoundException =>
        logThrowAndReThrow(fe, "FileName not found, " + system, LogLevel.ERROR)
        None
      case e: Exception =>
        logThrowAndReThrow(e, "Error Parsing, " + system, LogLevel.ERROR)
        None
    }
  }
}

object TypeSafeConfig extends ITypeSafeConfigSettings {
  protected val slf4jLogger = createLogger(this.getClass)
}

