package com.maubis.scarlet.base.support.specs

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.sections.Children
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.common.SingleComponentSection
import com.facebook.litho.widget.SolidColor
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R

object EmptySpec {
  fun create(context: ComponentContext): SolidColor.Builder {
    return SolidColor.create(context)
        .color(Color.TRANSPARENT)
  }
}

fun Drawable.color(tint: Int): Drawable {
  val drawableCopy = mutate().constantState?.newDrawable()
  drawableCopy?.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.MULTIPLY)
  return drawableCopy ?: this
}

fun Children.Builder.single(context: SectionContext, component: Component): Children.Builder {
  return child(SingleComponentSection.create(context).isFullSpan(true).component(component))
}

fun Children.Builder.single(context: SectionContext, component: Component.Builder<*>): Children.Builder {
  return child(SingleComponentSection.create(context).isFullSpan(true).component(component))
}

fun separatorSpec(context: ComponentContext): Component.Builder<*> {
  return SolidColor.create(context)
      .alignSelf(YogaAlign.CENTER)
      .colorRes(R.color.material_grey_200)
      .heightDip(1f)
      .widthDip(164f)
      .marginDip(YogaEdge.HORIZONTAL, 32f)
      .marginDip(YogaEdge.TOP, 12f)
      .marginDip(YogaEdge.BOTTOM, 12f)
}