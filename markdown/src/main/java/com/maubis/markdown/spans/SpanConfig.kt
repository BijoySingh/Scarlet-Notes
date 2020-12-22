package com.maubis.markdown.spans

import android.graphics.Color
import android.graphics.Typeface

class SpanConfig() {
  var codeTextColor: Int = Color.BLACK
  var codeBackgroundColor: Int = Color.GRAY
  var codeBlockLeadingMargin: Int = 0

  var quoteWidth: Int = 0
  var quoteColor: Int = Color.GRAY
  var quoteBlockLeadingMargin: Int = 0

  var separatorColor: Int = Color.BLACK
  var separatorWidth: Int = 0

  var headingTypeface: Typeface = Typeface.DEFAULT
  var heading2Typeface: Typeface = Typeface.DEFAULT
  var heading3Typeface: Typeface = Typeface.DEFAULT
  var textTypeface: Typeface = Typeface.DEFAULT
  var codeTypeface: Typeface = Typeface.MONOSPACE
}