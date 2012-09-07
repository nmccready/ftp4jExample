package com.nem.util

import org.slf4j.{LoggerFactory, Logger}

trait ILogger {
  protected val logger: Logger

  protected final def createLogger[T](cls: Class[T]): Logger = LoggerFactory.getLogger(cls)

  def buildThrowInfo(ex: Throwable, additonalInfo: String): String = {
    require(ex != null)
    StringsHelper.concatAll(additonalInfo, ex.getMessage)
  }

  def logThrowAndReThrow(ex: Throwable, additonalInfo: String, logLevel: LogLevel.Level): Unit = {
    logThrow(ex, additonalInfo, logLevel)
    throw ex
  }

  def logThrow(ex: Throwable, additonalInfo: String, logLevel: LogLevel.Level): Unit = {
    val error = buildThrowInfo(ex, additonalInfo)
    logLevel match {
      case LogLevel.DEBUG => logger.debug(error)
      case LogLevel.ERROR => logger.error(error)
      case LogLevel.INFO => logger.info(error)
      case LogLevel.CRITICAL => logger.error("CRITICAL Error: " + error)
      case LogLevel.FATAL => logger.error("FATAL Error: " + error)
      case LogLevel.TRACE => logger.trace(error)
      case LogLevel.WARNING => logger.warn(error)
      case _ => logger.error("Uknown Log Level (default error) : " + error)
    }
  }
}

object LogLevel extends Enumeration {
  type Level = Value
  val DEBUG, ERROR, INFO, CRITICAL, FATAL,TRACE,WARNING = Value
}
