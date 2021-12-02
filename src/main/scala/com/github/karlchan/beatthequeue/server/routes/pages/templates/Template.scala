package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.TypedTag
import scalatags.Text.all._

type Html = TypedTag[String]

object Template:
  def styledPage(contents: TypedTag[String]*): Html =
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
