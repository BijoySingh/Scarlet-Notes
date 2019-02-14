package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType

class WhatsNewBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.whats_new_title)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .text(WHATS_NEW_DETAILS_SUBTITLE)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_xlarge)
            .marginDip(YogaEdge.BOTTOM, 4f)
            .text(WHATS_NEW_DETAILS_NEW_FEATURES_TITLE)
            .typeface(FONT_MONSERRAT)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .text(Markdown.render(WHATS_NEW_DETAILS_NEW_FEATURES_MD, true))
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_xlarge)
            .marginDip(YogaEdge.BOTTOM, 4f)
            .text(WHATS_NEW_DETAILS_LAST_RELEASE_TITLE)
            .typeface(FONT_MONSERRAT)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .text(Markdown.render(WHATS_NEW_DETAILS_LAST_RELEASE_MD, true))
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(R.string.import_export_layout_exporting_done)
            .onPrimaryClick {
              dismiss()
            }
            .onSecondaryClick {
              val url = GOOGLE_TRANSLATE_URL + "en/" + Uri.encode(WHATS_NEW_DETAILS_NEW_FEATURES_MD)
              startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
              dismiss()
            }
            .secondaryActionRes(R.string.whats_new_translate)
            .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  companion object {
    val WHATS_NEW_UID = 9
    val GOOGLE_TRANSLATE_URL = "https://translate.google.com/#auto/"


    val WHATS_NEW_DETAILS_SUBTITLE = "A lot has changed in this update, here is a summary of those changes."
    val WHATS_NEW_DETAILS_NEW_FEATURES_TITLE = "New Features"
    val WHATS_NEW_DETAILS_LAST_RELEASE_TITLE = "Last Release"
    val WHATS_NEW_DETAILS_NEW_FEATURES_MD =
        "- **All New UI:** New Note and Settings UI. Cleaner, faster and built for easy use.\n\n" +
            "- **Easier Editor:** Easier and faster ways to get markdown, and section options.\n\n" +
            "- **Realtime Markdown:** When you type in markdown you get real time conversion and formatting.\n\n" +
            "- **More Editor Options:** Head over to settings to get more control on the editor experience.\n\n" +
            "- **More Themes:** Pro Users get more themes for the app, and the default dark theme is even darker now.\n\n" +
            "Even more little things which help you enjoy using this app everyday"
    val WHATS_NEW_DETAILS_LAST_RELEASE_MD =
        "- **New UI and Icon:** New Search and Top Actionbar UI and icon\n\n" +
            "- **Widgets:** Added a new list of notes widget. Also fixed widget not updating bug.\n\n" +
            "- **Reminder:** Improved reminders to be more reliable."


  }
}