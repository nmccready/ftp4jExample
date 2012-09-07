package com.nem.util

object TypeHelper {

  //http://stackoverflow.com/questions/7059451/writing-a-generic-cast-function-scala
  //getting around type erasure
  def cast[A: Manifest](value: Any): Option[A] = {
    val erasure = manifest[A] match {
      case Manifest.Byte => classOf[java.lang.Byte]
      case Manifest.Short => classOf[java.lang.Short]
      case Manifest.Char => classOf[java.lang.Character]
      case Manifest.Long => classOf[java.lang.Long]
      case Manifest.Float => classOf[java.lang.Float]
      case Manifest.Double => classOf[java.lang.Double]
      case Manifest.Boolean => classOf[java.lang.Boolean]
      case Manifest.Int => classOf[java.lang.Integer]
      case m => m.erasure
    }
    //If the instance is an Iterable Type or Collection type
    //erasure.isInstance does not guarantee a match. You really need to match on a single internal instance
    if (erasure.isInstance(value))
      Some(value.asInstanceOf[A])
    else None
  }

  def ->?->[T, R: Manifest](opts: Iterable[T]): Iterable[R] = convertWhatYouCan(opts)

  def convertWhatYouCan[T, R: Manifest](opts: Iterable[T]): Iterable[R] = {
    opts.foldLeft(List[R]())((a, b) => {
      val casted = TypeHelper.cast[R](b)
      if (casted.isDefined)
        casted.get :: a
      else
        a
    })
  }

  def ?->[T](opts: Iterable[Option[T]]): Iterable[T] = convertOptToNonOpt(opts)

  def convertOptToNonOpt[T](opts: Iterable[Option[T]]): Iterable[T] = {
    opts.foldLeft(List[T]())((a, b) => {
      if (b.isDefined)
        b.get :: a
      else
        a
    })
  }

  def /:?[T](passToFunc: Iterable[T], func: T => Option[T]): Iterable[T] = optionToFold(passToFunc, func)

  def optionToFold[T](passToFunc: Iterable[T], func: T => Option[T]): Iterable[T] = {
    passToFunc./:(List[T]())((a, b) => {
      val newEnt = func.apply(b)
      if (newEnt.isDefined)
        newEnt.get :: a
      else
        a
    })
  }

  def /:??[T](passToFunc: Iterable[T], func: T => Option[T]): Iterable[Option[T]] = foldItFromOptionFuncToOption(passToFunc, func)

  def foldItFromOptionFuncToOption[T](passToFunc: Iterable[T], func: T => Option[T]): Iterable[Option[T]] = {
    passToFunc./:(List[Option[T]]())((a, b) => {
      val newEnt = func.apply(b)
      newEnt :: a
    })
  }
}

