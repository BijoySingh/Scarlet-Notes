package com.maubis.scarlet.base.support.ui.font

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import com.maubis.scarlet.base.R

class TypefaceController(context: Context) {
  private val mFontMontserrat: Typeface = ResourcesCompat.getFont(context, R.font.monserrat) ?: Typeface.DEFAULT
  private val mFontMontserratMedium: Typeface = ResourcesCompat.getFont(context, R.font.monserrat_medium) ?: Typeface.DEFAULT
  private val mFontMontserratBold: Typeface = ResourcesCompat.getFont(context, R.font.monserrat_bold) ?: Typeface.DEFAULT
  private val mFontOpenSource: Typeface = ResourcesCompat.getFont(context, R.font.open_sans) ?: Typeface.DEFAULT

  fun heading(): Typeface = mFontMontserratBold

  fun subHeading(): Typeface = mFontMontserratMedium

  fun title(): Typeface = mFontMontserrat

  fun text(): Typeface = mFontOpenSource
}