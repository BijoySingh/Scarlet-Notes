package com.bijoysingh.quicknote.drive

import android.text.Layout
import com.facebook.litho.*
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
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object GDriveLogoutRootViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop loggingIn: Boolean): Component {
    val buttonTitle = when {
      loggingIn -> R.string.google_drive_page_logging_out_button
      else -> R.string.google_drive_page_logout_button
    }
    return Column.create(context)
        .backgroundColor(ApplicationBase.sAppTheme.get(ThemeColorType.BACKGROUND))
        .child(VerticalScroll.create(context)
            .flexGrow(1f)
            .marginDip(YogaEdge.ALL, 8f)
            .childComponent(GDriveLogoutContentView.create(context)))
        .child(Row.create(context)
            .backgroundRes(R.drawable.login_button_disabled)
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
            .clickHandler(GDriveLogoutRootView.onLogoutClick(context)))
        .build()
  }

  @OnEvent(ClickEvent::class)
  fun onLogoutClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object GDriveLogoutContentViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    return Column.create(context)
        .paddingDip(YogaEdge.ALL, 16f)
        .backgroundColor(ApplicationBase.sAppTheme.get(ThemeColorType.BACKGROUND))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_xxlarge)
            .textRes(R.string.google_drive_page_logout_title)
            .textColor(ApplicationBase.sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
            .typeface(CoreConfig.FONT_MONSERRAT_BOLD))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_large)
            .textColor(ApplicationBase.sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
            .textRes(R.string.google_drive_page_logout_details)
            .typeface(CoreConfig.FONT_MONSERRAT))
        .child(GDriveIconView.create(context)
            .marginDip(YogaEdge.TOP, 24f)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.icon_sync_disabled)
            .titleRes(R.string.google_drive_page_logout_no_sync_details))
        .child(GDriveIconView.create(context)
            .marginDip(YogaEdge.TOP, 16f)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.ic_restore)
            .titleRes(R.string.google_drive_page_logout_data_persists_details))
        .build()
  }
}
