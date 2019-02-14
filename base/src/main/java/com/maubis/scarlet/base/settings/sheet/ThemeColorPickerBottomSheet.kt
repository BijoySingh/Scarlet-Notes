package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeManager.Companion.getThemeFromStore
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.Flavor

@LayoutSpec
object ThemeColorPickerItemSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop theme: Theme,
               @Prop isDisabled: Boolean,
               @Prop isSelected: Boolean): Component {

    var icon = RoundIcon.create(context)
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
  fun onItemClick(context: ComponentContext,
                  @Prop theme: Theme,
                  @Prop isDisabled: Boolean,
                  @Prop onThemeSelected: (Theme) -> Unit) {
    if (isDisabled) {
      InstallProUpsellBottomSheet.openSheet(context.androidContext as ThemedActivity)
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
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.theme_page_title)
            .marginDip(YogaEdge.HORIZONTAL, 0f))

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
        CoreConfig.instance.appFlavor() == Flavor.PRO -> false
        theme == Theme.DARK || theme == Theme.LIGHT -> false
        else -> true
      }
      flex?.child(
          ThemeColorPickerItem.create(componentContext)
              .theme(theme)
              .isDisabled(disabled)
              .isSelected(theme.name == getThemeFromStore().name)
              .onThemeSelected { theme ->
                onThemeChange(theme)
                reset(componentContext.androidContext, dialog)
              }
              .flexGrow(1f))
    }
    column.child(flex)
    column.child(EmptySpec.create(componentContext).widthPercent(100f).heightDip(24f))
    column.child(BottomSheetBar.create(componentContext)
        .primaryActionRes(R.string.import_export_layout_exporting_done)
        .onPrimaryClick {
          dismiss()
        }.paddingDip(YogaEdge.VERTICAL, 8f))
    return column.build()
  }
}
