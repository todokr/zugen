package example.domain.pets

import example.domain.Id

/**
  * フワフワしたかわいい生き物
  */
case class Pet(
  id: Id[Pet],
  name: String,
  category: String,
  bio: String,
  status: PetStatus = Available,
  tags: Set[String] = Set.empty,
  photoUrls: Set[String] = Set.empty
)
