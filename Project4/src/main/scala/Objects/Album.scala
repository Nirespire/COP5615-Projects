package Objects

import java.util.Date


case class Album (
                   id:Integer,
                   from:Profile,
                   createdTime:Date,
                   updatedTime:Date,
                   coverPhoto:Picture,
                   description:String,
                   pictures:Array[Picture]
                 )