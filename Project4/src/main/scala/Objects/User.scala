package Objects

import akka.actor.Actor

case class User(id: Int,
                about: String,
                birthday: String,
                gender: Char,
                first_name: String,
                last_name: String)

