package com.bijoysingh.quicknote.firebase.activity

import android.graphics.Color
import android.graphics.drawable.Drawable
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
import com.facebook.litho.annotations.ResType
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.VerticalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.support.specs.color
import com.maubis.scarlet.base.support.ui.LithoCircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object FirebaseRootViewSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop loggingIn: Boolean): Component {
    val buttonTitle = when {
      loggingIn -> R.string.firebase_page_logging_in_button
      else -> R.string.firebase_page_login_button
    }
    return Column.create(context)
      .backgroundColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .child(
        VerticalScroll.create(context)
          .flexGrow(1f)
          .marginDip(YogaEdge.ALL, 8f)
          .childComponent(FirebaseContentView.create(context)))
      .child(
        Row.create(context)
          .backgroundRes(R.drawable.login_button_active)
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
              .typeface(sAppTypeface.title()))
          .clickHandler(FirebaseRootView.onGoogleClickEvent(context)))
      .build()
  }

  @OnEvent(ClickEvent::class)
  fun onGoogleClickEvent(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object FirebaseContentViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    return Column.create(context)
      .paddingDip(YogaEdge.ALL, 16f)
      .backgroundColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_xxlarge)
          .textRes(R.string.firebase_page_login_title)
          .textColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
          .typeface(sAppTypeface.heading()))
      .child(
        Text.create(context)
          .textSizeRes(R.dimen.font_size_large)
          .textColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
          .textRes(R.string.firebase_page_important_details)
          .typeface(sAppTypeface.title()))
      .child(
        FirebaseIconView.create(context)
          .marginDip(YogaEdge.TOP, 24f)
          .bgColorRes(R.color.dark_low_hint_text)
          .iconRes(R.drawable.icon_sync_disabled)
          .titleRes(R.string.firebase_page_not_sync_details))
      .child(
        FirebaseIconView.create(context)
          .bgColorRes(R.color.dark_low_hint_text)
          .iconRes(R.drawable.ic_delete_permanently)
          .titleRes(R.string.firebase_page_remove_details))
      .build()
  }
}

@LayoutSpec
object FirebaseIconViewSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop(resType = ResType.COLOR) bgColor: Int,
    @Prop(resType = ResType.DRAWABLE) icon: Drawable,
    @Prop(resType = ResType.STRING) title: String): Component {
    return Column.create(context)
      .paddingDip(YogaEdge.HORIZONTAL, 32f)
      .paddingDip(YogaEdge.VERTICAL, 24f)
      .child(
        Image.create(context)
          .drawable(icon.color(sAppTheme.get(ThemeColorType.SECONDARY_TEXT)))
          .background(LithoCircleDrawable(bgColor, Color.alpha(bgColor)))
          .paddingDip(YogaEdge.ALL, 12f)
          .marginDip(YogaEdge.BOTTOM, 12f)
          .heightDip(64f))
      .child(
        Text.create(context)
          .text(title)
          .textAlignment(Layout.Alignment.ALIGN_CENTER)
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
          .typeface(sAppTypeface.title()))
      .build()
  }
}
