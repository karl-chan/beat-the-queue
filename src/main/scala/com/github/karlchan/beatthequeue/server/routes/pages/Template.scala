package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.Tag._
import com.github.karlchan.beatthequeue.server.routes.pages.Widget._
import scalatags.Text.TypedTag
import scalatags.Text.all._

object Template:
  def styledPage(contents: TypedTag[String]*): TypedTag[String] =
    html(
      head(
        meta(charset := "UTF-8"),
        meta(
          name := "viewport",
          content := "width=device-width, initial-scale=1.0"
        ),
        link(
          href := "https://fonts.googleapis.com/icon?family=Material+Icons",
          rel := "stylesheet"
        ),
        link(
          href := "https://unpkg.com/tailwindcss@^2/dist/tailwind.min.css",
          rel := "stylesheet"
        )
      ),
      body(
        contents
      )
    )

object Tag:
  lazy val nav = tag("nav")

object Widget:
  def navigationBar =
    nav(
      cls := "flex items-center bg-gray-800 text-white",
      button(
        `type` := "button",
        cls := "p-4 text-gray-500 hover:bg-gray-900",
        span(cls := "material-icons", "menu")
      ),
      horizontalGap(8),
      linkButton("#", "Create alert", color = "red"),
      div(cls := "flex-grow"),
      linkButton("/logout", "Logout"),
      horizontalGap(8)
    )

  def horizontalGap(width: Int) = div(cls := s"w-$width")
  def verticalGap(height: Int) = div(cls := s"w-$height")

  def linkButton(
      destination: String,
      text: String,
      color: String = "gray"
  ) =
    a(
      href := destination,
      cls := s"px-3 py-2 rounded-md text-sm font-medium text-white bg-$color-600 hover:bg-$color-500 focus:ring-4 focus:ring-$color-100",
      text
    )
  def styledButton(text: String, buttonType: String, color: String = "gray") =
    button(
      cls := s"px-3 py-2 rounded-md text-sm font-medium text-white bg-$color-600 hover:bg-$color-500 focus:ring-4 focus:ring-$color-100",
      `type` := buttonType,
      text
    )
