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
          rel := "icon",
          `type` := "image/svg+xml",
          attr("sizes") := "any",
          href := "/static/icons/icon.svg"
        ),
        link(rel := "manifest", href := "static/manifest.json"),
        link(
          rel := "stylesheet",
          href := "https://fonts.googleapis.com/icon?family=Material+Icons"
        ),
        script(
          src := "https://cdn.tailwindcss.com"
        ),
        script(
          defer := true,
          src := "https://unpkg.com/alpinejs@3.x.x/dist/cdn.min.js"
        ),
        script(
          src := "/static/js/main.js"
        )
      ),
      body(
        contents
      )
    )
