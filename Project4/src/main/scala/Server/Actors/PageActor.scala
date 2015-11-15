package Server.Actors

import Objects.Page

class PageActor(page: Page) extends ProfileActor {
  profileId = page.id
}
