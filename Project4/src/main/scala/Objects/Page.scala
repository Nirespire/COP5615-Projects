package Objects


case class Page (
                 id:Integer,
                 about:String,
                 category:String,
                 cover:Picture,
                 likes:Integer
               ) extends Profile