package domain.customer

import domain.Id

/** A customer */
case class Customer(id: Id[Customer], name: CustomerName)
