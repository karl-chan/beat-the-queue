package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

object CriteriaPage:
  def catalog: Html =
    Template.styledPage(
      navigationBar,
      div(
        cls := "flex flex-col space-y-2",
        Merchants.All
          .map((category, merchants) =>
            div(
              h1(category),
              div(
                cls := "flex flex-wrap space-x-1",
                merchants
                  .map(merchant =>
                    linkButton(
                      color = "gray",
                      href := "/criteria/catalog",
                      merchant.name
                    )
                  )
              )
            )
          )
          .toSeq
      )
    )
