package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.merchants.CriteriaRenderer
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.given_Db
import scalatags.Text.all._

def homePage(authUser: AuthUser): IO[Html] =
  for {
    userCriteria <- CriteriaRenderer.forUser(authUser.id)
  } yield Template.styledPage(
    navigationBar,
    userCriteria
  )
