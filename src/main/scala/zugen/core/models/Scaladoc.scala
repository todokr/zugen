package zugen.core.models

/** A Scaladoc */
final case class Scaladoc(fileName: FileName, startLine: Int, endLine: Int, content: String) {

  def firstLine: String = content.split("\n").head
}
