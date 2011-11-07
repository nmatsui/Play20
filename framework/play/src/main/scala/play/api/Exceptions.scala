package play.api

import java.io.File

/** Helper for `PlayException`. */
object PlayException {

  private val generator = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis)

  /** Generates a new unique exception ID. */
  def nextId = java.lang.Long.toString(generator.incrementAndGet, 26)

  /** Adds source attachment to a Play exception. */
  trait ExceptionSource {
    self: PlayException =>

    /** Error line number, if defined. */
    def line: Option[Int]

    /** Column position, if defined. */
    def position: Option[Int]

    /** Input stream used to read the source content. */
    def input: Option[scalax.io.Input]

    def sourceName: Option[String]

    /** Extracts interesting lines to be displayed to the user.
      *
      * @param border number of lines to use as a border
      */
    def interestingLines(border: Int = 4): Option[(Int, Seq[String], Int)] = {
      for (f <- input; l <- line; val (first, last) = f.slurpString.split('\n').splitAt(l - 1); focus <- last.headOption) yield {
        val before = first.takeRight(border)
        val after = last.drop(1).take(border)
        val firstLine = l - before.size
        val errorLine = before.size
        (firstLine, (before :+ focus) ++ after, errorLine)
      }
    }

  }

  /** Adds any attachment to a Play exception. */
  trait ExceptionAttachment {
    self: PlayException =>

    /** Content title. */
    def subTitle: String

    /** Content to be displayed. */
    def content: String

  }

  /** Adds a rich description to a Play exception. */
  trait RichDescription {
    self: PlayException =>

    /** The new description formatted as HTML. */
    def htmlDescription: String

  }

}

/** Root exception for all Play problems.
  *
  * @param title the problem title
  * @param description the problem description
  * @param cause the underlying cause, if it exists
  */
case class PlayException(title: String, description: String, cause: Option[Throwable] = None) extends RuntimeException("%s [%s]".format(title, description), cause.orNull) {

  /** The exception ID, useful for retrieving problems in log files. */
  val id = PlayException.nextId

}

/** Generic exception for unexpected cases. */
case class UnexpectedException(message: Option[String] = None, unexpected: Option[Throwable] = None) extends PlayException(
  "Unexpected exception",
  message.getOrElse {
    unexpected.map(t => "%s: %s".format(t.getClass.getSimpleName, t.getMessage)).getOrElse("")
  },
  unexpected)

