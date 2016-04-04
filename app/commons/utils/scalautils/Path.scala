package commons.utils.scalautils

import play.api.mvc.RequestHeader

/**
  * Utilities related to handling URL paths
  */
object Path {

  implicit class PathOps(rh: RequestHeader) {

    def pathSegment(n: Int): Option[String] = {
      def nextSegment(i: Int, path: String): Option[String] = {
        i match {
          case 0 if path.length > 1 => Some(path.drop(1).takeWhile(_ != '/'))
          case _ if path.isEmpty => None
          case _ => nextSegment(i - 1, path.drop(1).dropWhile(_ != '/'))
        }
      }
      nextSegment(n,rh.path)
    }
  }

}
