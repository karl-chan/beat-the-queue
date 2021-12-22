package com.github.karlchan.beatthequeue.server.routes.pages.merchants

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.util.Reflection
import scalatags.Text.all._

object CriteriaRenderer:
  def render(criteria: Criteria[_]): Html =
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
