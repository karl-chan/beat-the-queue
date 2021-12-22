package com.github.karlchan.beatthequeue.server.routes.pages.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.Reflection
import mongo4cats.bson.ObjectId
import mongo4cats.collection.operations.Filter
import scalatags.Text.all._

object CriteriaRenderer:
  def forUser(userId: String)(using db: Db): IO[Html] =
    val categoryByMerchantNames = Merchants.All
      .flatMap((category, merchants) =>
        merchants.map(merchant => (merchant.name, category))
      )
      .toMap
    for {
      usersCollection <- db.users
      user <- usersCollection.find
        .filter(Filter.eq(Fields.Id, ObjectId(userId)))
        .first
        .map(_.get)
      criteriaByCategory = user.criteria.groupBy(c =>
        categoryByMerchantNames(c.merchant)
      )
    } yield div(
      cls := "flex flex-col space-y-2",
      criteriaByCategory
        .map((category, criteria) =>
          div(
            h1(category),
            div(
              cls := "flex flex-wrap space-x-1",
              criteria.map(render).toSeq
            )
          )
        )
        .toSeq
    )

  private def render(criteria: Criteria[_]): Html =
    val fields = Reflection.extractFields(criteria)
    table(
      tbody(
        for (field <- fields) yield {
          tr(
            td(field.name),
            td(field.value.toString)
          )
        }
      )
    )
