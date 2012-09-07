package com.nem.util

object StringsHelper {
  def concatAll(strings: String*): String = {

    val sb: StringBuilder = new StringBuilder()
    val ext = new StringBuilderExtensions(sb)
    ext.appendAllSeq(strings)
  }
}

object StringBuilderExtensions {
  implicit def StringBuilderExtensions(sb: StringBuilder) = new StringBuilderExtensions(sb)
}

class StringBuilderExtensions(sb: StringBuilder) {

  def appendAll(strings: String*): String = {
    this.appendAllSeq(strings)
  }

  def appendAllSeq(strings: Seq[String]): String = {
    strings.foreach(s => sb.append(s))
    sb.toString
  }
}
