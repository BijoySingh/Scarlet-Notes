package com.bijoysingh.quicknote.firebase.activity

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
import com.maubis.scarlet.base.support.specs.color
import com.maubis.scarlet.base.support.ui.LithoCircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object FirebaseRemovalRootViewSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop removingItems: Boolean): Component {
    val buttonTitle = when {
      removingItems -> R.string.firebase_removal_page_clearing_button
      else -> R.string.firebase_removal_page_clear_button
    }
    return Column.create(context)
        .backgroundColor(ApplicationBase.instance.themeController().get(ThemeColorType.BACKGROUND))
        .child(VerticalScroll.create(context)
            .flexGrow(1f)
            .marginDip(YogaEdge.ALL, 8f)
            .childComponent(FirebaseRemovalContentView.create(context)))
        .child(Row.create(context)
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
            .child(Text.create(context)
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
        .backgroundColor(ApplicationBase.instance.themeController().get(ThemeColorType.BACKGROUND))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_xxlarge)
            .textRes(R.string.firebase_removal_page_login_title)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
            .typeface(CoreConfig.FONT_MONSERRAT_BOLD))
        .child(Text.create(context)
            .textSizeRes(R.dimen.font_size_large)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
            .textRes(R.string.firebase_removal_page_important_details)
            .typeface(CoreConfig.FONT_MONSERRAT))
        .child(FirebaseIconView.create(context)
            .marginDip(YogaEdge.TOP, 24f)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.ic_delete_permanently)
            .titleRes(R.string.firebase_removal_page_remove_details))
        .child(FirebaseIconView.create(context)
            .bgColorRes(R.color.dark_low_hint_text)
            .iconRes(R.drawable.ic_action_backup)
            .titleRes(R.string.firebase_removal_page_next_details))
        .build()
  }
}