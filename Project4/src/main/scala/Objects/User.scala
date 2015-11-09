package Objects


case class User(
                 id:Integer,
                 about:String,
                 birthday:String,
                 gender: Char,
                 first_name:String,
                 last_name:String

               ) extends Profile
