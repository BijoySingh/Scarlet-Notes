package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.ui.LithoCircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.font.TypefaceController
import com.maubis.scarlet.base.support.ui.font.sPreferenceTypeface
import com.maubis.scarlet.base.support.utils.FlavorUtils

@LayoutSpec
object TypefacePickerItemSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop typeface: TypefaceController.TypefaceType,
    @Prop isDisabled: Boolean,
    @Prop isSelected: Boolean): Component {

    val typefaceSet = sAppTypeface.getSetForType(context.androidContext, typeface)
    val fontColor = when (isSelected) {
      true -> sAppTheme.get(ThemeColorType.ACCENT_TEXT)
      false -> sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    }
    val content = Column.create(context)
      .paddingDip(YogaEdge.ALL, 24f)
      .child(
        Row.create(context)
          .child(
            Text.create(context)
              .text("Abc")
              .textSizeRes(R.dimen.font_size_xlarge)
              .textColor(fontColor)
              .typeface(typefaceSet.heading))
          .child(
            Text.create(context)
              .text("defg")
              .textSizeRes(R.dimen.font_size_xlarge)
              .textColor(fontColor)
              .typeface(typefaceSet.subHeading)))
      .child(
        Text.create(context)
          .text("hijklmnop")
          .textSizeRes(R.dimen.font_size_large)
          .textColor(fontColor)
          .typeface(typefaceSet.title))
      .child(
        Text.create(context)
          .text("qrstuvwxyz\n0123456789")
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(fontColor)
          .typeface(typefaceSet.text))
      .background(LithoCircleDrawable(sAppTheme.get(ThemeColorType.BACKGROUND), 255, true))

    val data = Column.create(context)
      .alignSelf(YogaAlign.CENTER)
      .alignContent(YogaAlign.CENTER)
      .alignItems(YogaAlign.CENTER)
      .marginAuto(YogaEdge.HORIZONTAL)
      .child(content)
      .child(
        Text.create(context)
          .textRes(typeface.title)
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(sAppTheme.get(ThemeColorType.PRIMARY_TEXT))
          .typeface(sAppTypeface.title())
          .marginDip(YogaEdge.TOP, 12f))
    if (isDisabled) {
      data.alpha(0.4f)
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .alignContent(YogaAlign.CENTER)
      .child(data)
    row.clickHandler(ThemeColorPickerItem.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(
    context: ComponentContext,
    @Prop typeface: TypefaceController.TypefaceType,
    @Prop isDisabled: Boolean,
    @Prop onTypefaceSelected: (TypefaceController.TypefaceType) -> Unit) {
    if (isDisabled) {
      openSheet(context.androidContext as ThemedActivity, InstallProUpsellBottomSheet())
      return
    }
    onTypefaceSelected(typeface)
  }
}

class TypefacePickerBottomSheet : LithoBottomSheet() {

  var onTypefaceChange: (TypefaceController.TypefaceType) -> Unit = {}

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.typeface_page_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))

    var flex: Row.Builder? = null
    TypefaceController.TypefaceType.values().forEachIndexed { index, typeface ->
      if (index % 2 == 0) {
        column.child(flex)
        flex = Row.create(componentContext)
          .widthPercent(100f)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.VERTICAL, 12f)
      }
      flex?.child(
        TypefacePickerItem.create(componentContext)
          .typeface(typeface)
          .isDisabled(FlavorUtils.isLite() && !typeface.isLiteEnabled)
          .isSelected(sPreferenceTypeface == typeface.name)
          .onTypefaceSelected { newTypeface -> onTypefaceChange(newTypeface) }
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
