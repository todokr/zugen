package example.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.Digit
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.generic.Equal

package object literaltypes {
  type Code = String Refined (Forall[Digit] And Size[Equal[9]])
}
