package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.maybeThrow

class AboutUsBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity

    var version = ""
    try {
      val pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
      version = pInfo.versionName
    } catch (exception: Exception) {
      maybeThrow(activity, exception)
    }

    val appName = getString(R.string.app_name)
    val aboutUsDetails = getString(R.string.about_page_about_us_details, appName)
    val aboutAppDetails = getString(R.string.about_page_description, appName)

    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.home_option_about_page)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(aboutUsDetails)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.about_page_about_app)
          .typeface(sAppTypeface.title())
          .textColor(sAppTheme.get(ThemeColorType.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(aboutAppDetails)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.about_page_app_version)
          .typeface(sAppTypeface.title())
          .textColor(sAppTheme.get(ThemeColorType.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(version)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.about_page_rate)
               .onPrimaryClick {
                 try {
                   IntentUtils.openAppPlayStore(activity)
                   dismiss()
                 } catch (exception: Exception) {
                   maybeThrow(activity, exception)
                 }
               }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}