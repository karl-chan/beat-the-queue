package com.github.karlchan.beatthequeue.server.routes.pages.templates.merchants

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

class CriteriaFormBuilder:
  def render[M](merchant: String, criteria: Criteria[M]): Html =
    form(
      action := "/criteria/create",
      method := "POST",
      input(`type` := "hidden", name := "merchant", value := merchant),
      criteria.fields
        .map((fieldName, inputField) => inputField.render(fieldName))
        .toSeq,
      styledButton(color = "green", `type` := "submit", "Submit")
    )
