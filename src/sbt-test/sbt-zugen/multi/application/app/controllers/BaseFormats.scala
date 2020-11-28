package controllers

import domain.Id
import play.api.libs.json.{Reads, Writes}

trait BaseFormats {
  implicit def idReads[T]: Reads[Id[T]] = Reads.StringReads.map(Id[T])
  implicit def idWrites[T]: Writes[Id[T]] = Writes.StringWrites.contramap(_.value)
}
