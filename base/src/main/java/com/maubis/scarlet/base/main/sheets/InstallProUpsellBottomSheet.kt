package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.GridSectionItem
import com.maubis.scarlet.base.support.specs.GridSectionOptionItem
import com.maubis.scarlet.base.support.specs.GridSectionView
import com.maubis.scarlet.base.support.ui.ThemeColorType

class InstallProUpsellBottomSheet : LithoBottomSheet() {
  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val options = listOf(
        GridSectionOptionItem(R.drawable.ic_whats_new, R.string.install_pro_sheet_latest_updates, {}),
        GridSectionOptionItem(R.drawable.ic_action_lock, R.string.install_pro_sheet_app_lock, {}),
        GridSectionOptionItem(R.drawable.ic_action_day_mode, R.string.install_pro_sheet_app_themes, {}),
        GridSectionOptionItem(R.drawable.ic_title_white_48dp, R.string.install_pro_sheet_font_size, {}),
        GridSectionOptionItem(R.drawable.ic_action_color, R.string.install_pro_sheet_viewer_bg, {}),
        GridSectionOptionItem(R.drawable.icon_widget, R.string.install_pro_sheet_widget_options, {}))

    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.available_in_pro_only)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .textRes(R.string.why_install_pro)
            .typeface(FONT_MONSERRAT)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(GridSectionView.create(componentContext)
            .maxLines(3)
            .numColumns(2)
            .iconSizeRes(R.dimen.primary_round_icon_size)
            .section(GridSectionItem(options = options))
            .showSeparator(false))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(R.string.install_pro_app)
            .onPrimaryClick {
              IntentUtils.openAppPlayStore(activity, "com.bijoysingh.quicknote.pro")
              dismiss()
            }
            .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}