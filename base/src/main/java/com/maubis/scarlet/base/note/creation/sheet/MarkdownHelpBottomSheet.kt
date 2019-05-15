package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.ui.ThemeColorType

class MarkdownHelpBottomSheet : LithoBottomSheet() {
  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .child(getLithoBottomSheetTitle(componentContext).textRes(R.string.markdown_help_sheet_title))

    val examples = arrayOf("# Heading", "## Sub Heading", "```\nblock of code\n```",  "> quoted text", "**bold**", "*italics*", "_underline_", "~~strike through~~", "`piece of code`")
    examples.forEach {
      column
          .child(Text.create(componentContext)
              .text(Markdown.render(it))
              .textSizeRes(R.dimen.font_size_normal)
              .marginDip(YogaEdge.HORIZONTAL, 20f)
              .paddingDip(YogaEdge.VERTICAL, 4f)
              .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT)))
    }

    return column.build()
  }
}