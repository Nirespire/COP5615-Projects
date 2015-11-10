package Objects


abstract class Profile {
  def id: Integer

  override def toString(): String ={
    "Profile id " + id
  }
}
