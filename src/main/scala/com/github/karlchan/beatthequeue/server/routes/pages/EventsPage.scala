package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Event
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import io.circe.generic.auto._
import io.circe.syntax._
import scalatags.Text.all._

object EventsPage:
  def render(authUser: AuthUser)(using db: Db): IO[Html] =
    for {
      dbUser <- db.findUser(authUser)
      sortedMerchantEvents = dbUser.events.sortBy(_.time).groupBy(_.merchant)
    } yield Template.styledPageWithNav(
      div(
        cls := "flex flex-col space-y-2",
        h1(cls := "text-4xl text-gray-800 font-bold", "Upcoming events"),
        vspace(4),
        if dbUser.events.isEmpty then
          div(cls := "mx-auto text-gray", "You have no upcoming events.")
        else
          ul(
            cls := "list-disc",
            sortedMerchantEvents
              .flatMap((name, merchantEvents) => {
                val merchant = Merchants.AllByName(name)
                Seq(
                  li(
                    img(
                      cls := "w-32 bg-gray-200",
                      src := merchant.logoUrl,
                      alt := name
                    )
                  ),
                  div(
                    cls := "grid grid-cols-4 gap-4",
                    merchantEvents.map(renderEvent(_, name))
                  )
                )
              })
              .toSeq
          )
      )
    )

  private def renderEvent[M](
      event: Event[M],
      merchantName: String
  ): Html =
    val merchant =
      Merchants.AllByName(merchantName).asInstanceOf[Merchant[M, _, Event[M]]]
    merchant.renderer.render(event)
