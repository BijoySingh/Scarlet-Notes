package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.maybeThrow

class OpenSourceBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val appName = componentContext.getString(R.string.app_name)
    val creatorName = componentContext.getString(R.string.maubis_apps)
    val openSourceDetails = getString(R.string.about_page_description_os, appName, creatorName)
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.osp_page_about_osp)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .text(openSourceDetails)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .textRes(R.string.osp_page_libraries)
          .typeface(CoreConfig.FONT_MONSERRAT)
          .textColor(sAppTheme.get(ThemeColorType.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .marginDip(YogaEdge.BOTTOM, 4f)
          .text(Markdown.render(LIBRARY_DETAILS_MD, true))
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.about_page_contribute)
               .onPrimaryClick {
                 try {
                   activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                   dismiss()
                 } catch (exception: Exception) {
                   maybeThrow(activity, exception)
                 }
               }
               .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  companion object {
    val GITHUB_URL = "https://github.com/BijoySingh/Material-Notes-Android-App"
    val LIBRARY_DETAILS_MD = "**Android Support Libraries**\n" +
      "- `'com.android.support.appcompat-v7'`\n" +
      "- `'com.android.support.recyclerview-v7'`\n" +
      "- `'com.android.support.cardview-v7'`\n" +
      "- `'com.android.support.support-v4'`\n" +
      "- `'com.android.support.design'`\n" +
      "- `'com.android.support.constraint'`\n\n" +
      "**Android Architecture Room Library**\n" +
      "- `'android.arch.persistence.room'`\n\n" +
      "**Internal Support Libraries**\n" +
      "- `'com.github.bijoysingh.android-basics'`\n" +
      "- `'com.github.bijoysingh.ui-basics'`\n" +
      "- `'com.github.bijoysingh.floating-bubble'`\n\n" +
      "**Kotlin Support**\n" +
      "- `'org.jetbrains.kotlin'`\n" +
      "- `'org.jetbrains.kotlinx'`\n\n" +
      "**Reprint: Fingerprint Library**\n" +
      "- `'com.github.ajalt.reprint'`\n\n" +
      "**Google Firebase Support Library**\n" +
      "- `'com.google.firebase:firebase-auth'`\n" +
      "- `'com.google.firebase:firebase-database'`\n\n" +
      "**Google Play Services Library**\n" +
      "- `'com.google.android.gms:play-services-auth'`\n\n" +
      "**Shortcuts Gradle Plugin**\n" +
      "- `'com.github.zellius:android-shortcut-gradle-plugin'`\n\n" +
      "**Facebook Litho and SoLoader**\n" +
      "- `'com.facebook.litho'`\n" +
      "- `'com.facebook.soloader:soloader'`\n\n" +
      "**Easy Image**\n" +
      "- `'com.github.jkwiecien:EasyImage'`\n\n" +
      "**Evernote Android Job**\n" +
      "- `'com.evernote:android-job'`\n\n" +
      "**Google Flexbox Library**\n" +
      "- `'com.google.android:flexbox'`\n"

  }
}