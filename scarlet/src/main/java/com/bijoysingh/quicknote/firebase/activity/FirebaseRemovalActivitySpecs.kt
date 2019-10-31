package com.bijoysingh.quicknote.firebase.activity

import android.text.Layout
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.VerticalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object FirebaseRemovalRootViewSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop removingItems: Boolean): Component {
    val buttonTitle = when {
      removingItems -> R.string.firebase_removal_page_clearing_button
      else -> R.string.firebase_removal_page_clear_button
    }
    return Column.create(context)
      .backgroundColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .child(
        VerticalScroll.create(context)
          .flexGrow(1f)
          .marginDip(YogaEdge.ALL, 8f)
          .childComponent(FirebaseRemovalContentView.create(context)))
      .child(
        Row.create(context)
          .backgroundRes(R.drawable.login_button_disabled)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.HORIZONTAL, 12f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .marginDip(YogaEdge.ALL, 16f)
          .child(
            Image.create(context)
              .drawableRes(R.drawable.ic_google_icon)
              .heightDip(36f)
          )
          .child(
            Text.create(context)
              .textSizeRes(R.dimen.font_size_large)
              .textColorRes(R.color.white)
              .textRes(buttonTitle)
              .textAlignment(Layout.Alignment.ALIGN_CENTER)
              .flexGrow(1f)
              .typeface(CoreConfig.FONT_MONSERRAT))
          .clickHandler(FirebaseRemovalRootView.onLogoutClickEvent(context)))
      .build()
  }

  @OnEvent(ClickEvent::class)
  fun onLogoutClickEvent(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object FirebaseRemovalContentViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    return Column.create(context)
      .paddingDip(YogaEdge.ALL, 16f)
      .backgroundColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_xxlarge)
          .textRes(R.string.firebase_removal_page_login_title)
          .textColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
          .typeface(CoreConfig.FONT_MONSERRAT_BOLD))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_large)
          .textColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
          .textRes(R.string.firebase_removal_page_important_details)
          .typeface(CoreConfig.FONT_MONSERRAT))
      .child(
        Row.create(context)
          .marginDip(YogaEdge.TOP, 24f)
          .child(
            FirebaseIconView.create(context)
              .bgColorRes(R.color.dark_low_hint_text)
              .iconRes(R.drawable.ic_privacy_policy)
              .titleRes(R.string.firebase_removal_page_more_privacy_details)
              .flexGrow(1f))
          .child(
            FirebaseIconView.create(context)
              .bgColorRes(R.color.dark_low_hint_text)
              .iconRes(R.drawable.ic_image_gallery)
              .titleRes(R.string.firebase_removal_page_photo_upload_details)
              .flexGrow(1f))
      )
      .child(
        Text.create(context)
          .marginDip(YogaEdge.HORIZONTAL, 16f)
          .paddingDip(YogaEdge.BOTTOM, 10f)
          .paddingDip(YogaEdge.TOP, 20f)
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
          .textRes(R.string.firebase_removal_page_whats_next_details)
          .typeface(CoreConfig.FONT_MONSERRAT_BOLD))
      .child(
        Text.create(context)
          .backgroundRes(R.drawable.secondary_rounded_bg)
          .marginDip(YogaEdge.VERTICAL, 4f)
          .marginDip(YogaEdge.HORIZONTAL, 16f)
          .paddingDip(YogaEdge.ALL, 12f)
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
          .textRes(R.string.firebase_removal_page_remove_details)
          .typeface(CoreConfig.FONT_MONSERRAT))
      .child(
        Text.create(context)
          .backgroundRes(R.drawable.secondary_rounded_bg)
          .marginDip(YogaEdge.VERTICAL, 4f)
          .marginDip(YogaEdge.HORIZONTAL, 16f)
          .paddingDip(YogaEdge.ALL, 12f)
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
          .textRes(R.string.firebase_removal_page_next_details)
          .typeface(CoreConfig.FONT_MONSERRAT))
      .build()
  }
}