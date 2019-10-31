package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.GridSectionItem
import com.maubis.scarlet.base.support.specs.GridSectionOptionItem
import com.maubis.scarlet.base.support.specs.GridSectionView
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.FlavorUtils
import com.maubis.scarlet.base.support.utils.OsVersionUtils

const val WHATS_NEW_SHEET_INDEX = 11

class WhatsNewBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val options = listOf(
      if (FlavorUtils.isOpenSource()) null else GridSectionOptionItem(R.drawable.gdrive_icon, R.string.whats_new_sheet_google_drive, {}),
      GridSectionOptionItem(R.drawable.icon_share_image, R.string.whats_new_sheet_photo_share, {}),
      GridSectionOptionItem(R.drawable.ic_action_color, R.string.whats_new_sheet_note_color, {}),
      GridSectionOptionItem(R.drawable.icon_languages, R.string.whats_new_sheet_more_languages, {}),
      if (!OsVersionUtils.canAddLauncherShortcuts()) null else GridSectionOptionItem(
        R.drawable.icon_shortcut, R.string.whats_new_sheet_launcher_shortcuts, {}),
      GridSectionOptionItem(R.drawable.ic_markdown_logo, R.string.whats_new_sheet_markdown_improvements, {}),
      GridSectionOptionItem(R.drawable.icon_widget, R.string.whats_new_sheet_ui_improvements, {}))

    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.whats_new_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textRes(R.string.whats_new_sheet_subtitle)
          .typeface(FONT_MONSERRAT)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        GridSectionView.create(componentContext)
          .maxLines(3)
          .numColumns(2)
          .iconSizeRes(R.dimen.ultra_large_round_icon_size)
          .section(GridSectionItem(options = options.filterNotNull()))
          .showSeparator(false))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.import_export_layout_exporting_done)
               .onPrimaryClick {
                 dismiss()
               }
               .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}