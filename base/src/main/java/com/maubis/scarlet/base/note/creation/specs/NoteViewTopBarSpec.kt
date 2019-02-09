package com.maubis.scarlet.base.note.creation.specs

import android.graphics.Color
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.note.creation.activity.NoteViewColorConfig
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon

fun topBarIcon(context: ComponentContext,
               colorConfig: NoteViewColorConfig): RoundIcon.Builder {
  return RoundIcon.create(context)
      .bgColor(Color.TRANSPARENT)
      .iconColor(colorConfig.toolbarIconColor)
      .iconSizeRes(R.dimen.toolbar_round_icon_size)
      .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
      .iconMarginVerticalRes(R.dimen.toolbar_round_icon_margin_vertical)
      .iconMarginHorizontalRes(R.dimen.toolbar_round_icon_margin_horizontal)
      .bgAlpha(15)
}

@LayoutSpec
object NoteViewTopBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: NoteViewColorConfig): Component {
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
    row.child(EmptySpec.create(context).heightDip(10f))
    return row.build()
  }
}

@LayoutSpec
object NoteCreationTopBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: NoteViewColorConfig): Component {
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
    row.child(EmptySpec.create(context).heightDip(10f))
    return row.build()
  }
}
