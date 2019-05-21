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
import com.maubis.scarlet.base.config.ApplicationBase
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
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_xlarge)
            .marginDip(YogaEdge.BOTTOM, 4f)
            .text(WHATS_NEW_DETAILS_NEW_FEATURES_TITLE)
            .typeface(FONT_MONSERRAT)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .text(Markdown.render(WHATS_NEW_DETAILS_NEW_FEATURES_MD, true))
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_xlarge)
            .marginDip(YogaEdge.BOTTOM, 4f)
            .text(WHATS_NEW_DETAILS_COMING_SOON_TITLE)
            .typeface(FONT_MONSERRAT)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .text(Markdown.render(WHATS_NEW_DETAILS_COMING_SOON_MD, true))
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
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
    val WHATS_NEW_UID = 10
    val GOOGLE_TRANSLATE_URL = "https://translate.google.com/#auto/"

    val WHATS_NEW_DETAILS_SUBTITLE = "A lot has changed in this update, here is a summary of those changes."
    val WHATS_NEW_DETAILS_NEW_FEATURES_TITLE = "New Features"
    val WHATS_NEW_DETAILS_COMING_SOON_TITLE = "Coming Soon"
    val WHATS_NEW_DETAILS_NEW_FEATURES_MD =
        "- **Checked Items:** You can now enable / disable checked items from moving down when checked.\n\n" +
        "- **Bug Fixes:** This release fixes multiple crash issues throughout the application.\n\n" +
            "Even more little things which help you enjoy using this app everyday"
    val WHATS_NEW_DETAILS_COMING_SOON_MD =
        "- **Google Drive Based Sync:** We are building a secure and private Google Drive based sync\n\n" +
            "- **Photo Sync:** Drive sync will also allow syncing photos between devices as well.\n\n"


  }
}