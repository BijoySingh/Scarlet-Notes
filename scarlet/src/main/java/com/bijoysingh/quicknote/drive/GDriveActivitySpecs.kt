package com.bijoysingh.quicknote.drive

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Layout
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.VerticalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.support.specs.color
import com.maubis.scarlet.base.support.ui.LithoCircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object GDriveRootViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop loggingIn: Boolean): Component {
    val buttonTitle = when {
      loggingIn -> R.string.google_drive_page_logging_in_button
      else -> R.string.google_drive_page_login_button
    }
    return Column.create(context)
        .backgroundColor(ApplicationBase.instance.themeController().get(ThemeColorType.BACKGROUND))
        .child(VerticalScroll.create(context)
            .flexGrow(1f)
            .marginDip(YogaEdge.ALL, 8f)
            .childComponent(GDriveContentView.create(context)))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_large)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.PRIMARY_TEXT))
            .textRes(R.string.google_drive_page_login_firebase_button)
            .textAlignment(Layout.Alignment.ALIGN_CENTER)
            .typeface(CoreConfig.FONT_MONSERRAT))
        .child(Row.create(context)
            .backgroundRes(R.drawable.accent_rounded_bg)
            .alignItems(YogaAlign.CENTER)
            .paddingDip(YogaEdge.HORIZONTAL, 12f)
            .paddingDip(YogaEdge.VERTICAL, 8f)
            .marginDip(YogaEdge.ALL, 16f)
            .child(
                Image.create(context)
                    .drawableRes(R.drawable.gdrive_icon)
                    .heightDip(36f)
            )
            .child(Text.create(context)
                .textSizeRes(R.dimen.font_size_large)
                .textColorRes(R.color.white)
                .textRes(buttonTitle)
                .textAlignment(Layout.Alignment.ALIGN_CENTER)
                .flexGrow(1f)
                .typeface(CoreConfig.FONT_MONSERRAT))
            .clickHandler(GDriveRootView.onGoogleClickEvent(context)))
        .build()
  }

  @OnEvent(ClickEvent::class)
  fun onGoogleClickEvent(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object GDriveContentViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    return Column.create(context)
        .paddingDip(YogaEdge.ALL, 16f)
        .backgroundColor(ApplicationBase.instance.themeController().get(ThemeColorType.BACKGROUND))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_xxlarge)
            .textRes(R.string.google_drive_page_login_title)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
            .typeface(CoreConfig.FONT_MONSERRAT_BOLD))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_large)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
            .textRes(R.string.google_drive_page_login_details)
            .typeface(CoreConfig.FONT_MONSERRAT))
        .child(GDriveIconView.create(context)
            .marginDip(YogaEdge.TOP, 24f)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.ic_action_lock)
            .titleRes(R.string.google_drive_page_login_lock_details))
        .child(GDriveIconView.create(context)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.ic_image_gallery)
            .titleRes(R.string.google_drive_page_photo_details))
        .build()
  }
}

@LayoutSpec
object GDriveIconViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) bgColor: Int,
               @Prop(resType = ResType.DRAWABLE) icon: Drawable,
               @Prop(resType = ResType.STRING) title: String): Component {
    return Column.create(context)
        .paddingDip(YogaEdge.HORIZONTAL, 32f)
        .paddingDip(YogaEdge.VERTICAL, 24f)
        .child(Image.create(context)
            .drawable(icon.color(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT)))
            .background(LithoCircleDrawable(bgColor, Color.alpha(bgColor)))
            .paddingDip(YogaEdge.ALL, 12f)
            .marginDip(YogaEdge.BOTTOM, 12f)
            .heightDip(64f))
        .child(Text.create(context)
            .text(title)
            .textAlignment(Layout.Alignment.ALIGN_CENTER)
            .textSizeRes(R.dimen.font_size_normal)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
            .typeface(CoreConfig.FONT_MONSERRAT))
        .build()
  }
}
