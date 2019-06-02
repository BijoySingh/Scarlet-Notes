package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.util.Log
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
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType
import android.content.Intent
import android.net.Uri

class ExceptionBottomSheet : LithoBottomSheet() {
  var exception: Exception = RuntimeException()

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.exception_sheet_title)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_small)
            .text(Markdown.render("```\n${Log.getStackTraceString(exception)}\n```", true))
            .marginDip(YogaEdge.BOTTOM, 16f)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(R.string.exception_sheet_crash_app)
            .onPrimaryClick {
              throw exception
            }.secondaryActionRes(R.string.exception_sheet_mail)
            .onSecondaryClick {
              try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:team.thecodershub@gmail.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "[Exception] The application threw an exception")
                intent.putExtra(Intent.EXTRA_TEXT, "Hi, my app threw this exception\n${Log.getStackTraceString(exception)}")
                startActivity(Intent.createChooser(intent, "Send email to developer..."))
              } catch (exception: Exception) {
                // Ignore this one ;)
              }
              dismiss()
            }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}