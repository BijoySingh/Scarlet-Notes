package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity

class InstallProUpsellBottomSheet : LithoBottomSheet() {
  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as ThemedActivity
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.available_in_pro_only)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 4f)
            .textRes(R.string.why_install_pro)
            .typeface(FONT_MONSERRAT)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .textRes(R.string.why_install_pro_details)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(R.string.install_pro_app)
            .onPrimaryClick {
              IntentUtils.openAppPlayStore(activity, "com.bijoysingh.quicknote.pro")
              dismiss()
            }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}