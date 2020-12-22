package com.maubis.scarlet.base.note.creation.specs

import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.yoga.YogaAlign
import com.maubis.scarlet.base.support.specs.EmptySpec

@LayoutSpec
object NoteViewTopBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
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
  fun onCreate(context: ComponentContext): Component {
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
    row.child(EmptySpec.create(context).heightDip(10f))
    return row.build()
  }
}
