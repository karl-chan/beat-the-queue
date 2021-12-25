package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.given_Db
import mongo4cats.bson.ObjectId
import mongo4cats.collection.operations.Filter
import scalatags.Text.all._

object HomePage:
  def render(authUser: AuthUser): IO[Html] =
    for {
      criteria <- renderCriteria(authUser)
    } yield Template.styledPage(
      navigationBar,
      criteria
    )

  private def renderCriteria(authUser: AuthUser)(using db: Db): IO[Html] =
    val categoryByMerchantNames = Merchants.All
      .flatMap((category, merchants) =>
        merchants.map(merchant => (merchant.name, category))
      )
      .toMap
    for {
      user <- db.findUser(authUser)
      criteriaByCategory = user.criteria.groupBy(c =>
        categoryByMerchantNames(c.merchant)
      )
    } yield div(
      cls := "flex flex-col space-y-2",
      h1(cls := "text-4xl text-gray-800 font-bold", "Alerts"),
      verticalGap(4),
      if user.criteria.isEmpty then
        div(cls := "mx-auto text-gray", "You have not set up alerts.")
      else
        criteriaByCategory
          .map((category, criteriaSeq) =>
            div(
              h2(cls := "text-2xl text-gray-800 font-semibold", category),
              div(
                cls := "flex flex-wrap space-x-1",
                criteriaSeq
                  .map(criteria =>
                    val merchant = Merchants
                      .findMerchantFor(criteria)
                    merchant.renderer
                      .render(criteria)(using merchant.criteriaEncoder)
                  )
                  .toSeq
              )
            )
          )
          .toSeq
    )
