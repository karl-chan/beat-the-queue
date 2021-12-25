package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.TypedTag
import scalatags.Text.all._

type Html = TypedTag[String]

object Template:
  def styledPage(contents: Html*): Html =
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
        script(
          src := "https://cdn.tailwindcss.com"
        ),
        script(
          defer := true,
          src := "https://unpkg.com/alpinejs@3.x.x/dist/cdn.min.js"
        )
      ),
      body(
        contents
      )
    )
