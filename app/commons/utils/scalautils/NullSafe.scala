package commons.utils.scalautils

class NullSafe[A](expr: => A) {

  def getOrElse(default: A) = try {
    if (expr == null)
      default
    else
      expr
  } catch {
    case npe: NullPointerException => default
    case nfe: NumberFormatException => default
    case nse: NoSuchElementException => default
  }
}

object NullSafe {
  def apply[A](expr: => A) = {
    new NullSafe[A](expr)
  }
}
