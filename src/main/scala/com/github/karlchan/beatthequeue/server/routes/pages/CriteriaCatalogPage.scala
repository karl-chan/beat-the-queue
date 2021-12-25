package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

object CriteriaCatalogPage:
  def render: Html =
    Template.styledPage(
      navigationBar,
      Renderer.renderCatalog()
    )
