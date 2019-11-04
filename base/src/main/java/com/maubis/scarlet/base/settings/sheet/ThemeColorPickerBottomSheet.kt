package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeManager.Companion.getThemeFromStore
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.sThemeIsAutomatic
import com.maubis.scarlet.base.support.ui.setThemeFromSystem
import com.maubis.scarlet.base.support.utils.FlavorUtils
import com.maubis.scarlet.base.support.utils.OsVersionUtils

@LayoutSpec
object ThemeColorPickerItemSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop theme: Theme,
    @Prop isDisabled: Boolean,
    @Prop isSelected: Boolean): Component {

    val icon = RoundIcon.create(context)
      .showBorder(true)
      .iconSizeDip(64f)
      .iconPaddingDip(16f)
      .onClick { }
      .flexGrow(1f)
      .isClickDisabled(true)
      .alpha(if (isDisabled) 0.3f else 1f)
    when (isSelected) {
      true -> icon.iconRes(R.drawable.ic_done_white_48dp)
        .bgColorRes(R.color.colorAccent)
        .iconColor(Color.WHITE)
      false -> icon.iconRes(R.drawable.icon_realtime_markdown)
        .bgColorRes(theme.background)
        .iconColorRes(theme.primaryText)
    }
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .child(icon)
    row.clickHandler(ThemeColorPickerItem.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(
    context: ComponentContext,
    @Prop theme: Theme,
    @Prop isDisabled: Boolean,
    @Prop onThemeSelected: (Theme) -> Unit) {
    if (isDisabled) {
      openSheet(context.androidContext as ThemedActivity, InstallProUpsellBottomSheet())
      return
    }
    onThemeSelected(theme)
  }
}

class ThemeColorPickerBottomSheet : LithoBottomSheet() {

  var onThemeChange: (Theme) -> Unit = {}

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.theme_page_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))

    if (OsVersionUtils.canUseSystemTheme()) {
      column.child(OptionItemLayout.create(componentContext)
                     .option(
                       LithoOptionsItem(
                         title = R.string.theme_use_system_theme,
                         subtitle = R.string.theme_use_system_theme_details,
                         icon = R.drawable.ic_action_color,
                         listener = {},
                         isSelectable = true,
                         selected = sThemeIsAutomatic,
                         actionIcon = if (FlavorUtils.isLite()) R.drawable.ic_rating else 0
                       ))
                     .onClick {
                       val context = componentContext.androidContext as AppCompatActivity
                       if (FlavorUtils.isLite()) {
                         openSheet(context, InstallProUpsellBottomSheet())
                         return@onClick
                       }

                       sThemeIsAutomatic = !sThemeIsAutomatic
                       if (sThemeIsAutomatic) {
                         setThemeFromSystem(context)
                         onThemeChange(sAppTheme.get())
                       }
                       reset(context, dialog)
                     })
    }

    if (!sThemeIsAutomatic) {
      var flex: Row.Builder? = null
      Theme.values().forEachIndexed { index, theme ->
        if (index % 4 == 0) {
          column.child(flex)
          flex = Row.create(componentContext)
            .widthPercent(100f)
            .alignItems(YogaAlign.CENTER)
            .paddingDip(YogaEdge.VERTICAL, 12f)
        }

        val disabled = when {
          !FlavorUtils.isLite() -> false
          theme == Theme.DARK || theme == Theme.LIGHT -> false
          else -> true
        }
        flex?.child(
          ThemeColorPickerItem.create(componentContext)
            .theme(theme)
            .isDisabled(disabled)
            .isSelected(theme.name == getThemeFromStore().name)
            .onThemeSelected { newTheme ->
              onThemeChange(newTheme)
            }
            .flexGrow(1f))
      }
      column.child(flex)
    }

    column.child(EmptySpec.create(componentContext).widthPercent(100f).heightDip(24f))
    column.child(BottomSheetBar.create(componentContext)
                   .primaryActionRes(R.string.import_export_layout_exporting_done)
                   .onPrimaryClick {
                     dismiss()
                   }.paddingDip(YogaEdge.VERTICAL, 8f))
    return column.build()
  }
}
