package Objects

case class User(
                 baseObject: BaseObject,
                 about: String,
                 birthday: String,
                 gender: Char,
                 first_name: String,
                 last_name: String,
                 publicKey: Array[Byte]
               )