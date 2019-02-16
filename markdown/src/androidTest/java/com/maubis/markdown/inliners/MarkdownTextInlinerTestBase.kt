package com.maubis.markdown.inliners

import android.util.Log
import com.maubis.markdown.inliner.MarkdownInline
import org.junit.Assert

abstract class MarkdownTextInlinerTestBase {
  protected fun assert(expectedInline: MarkdownInline, processedInline: MarkdownInline) {
    Log.d("Asset:started..", "-----------------")
    Log.d("Asset:expected.", expectedInline.debug())
    Log.d("Asset:processed", processedInline.debug())

    Assert.assertEquals("Expected equals", expectedInline.debug(), processedInline.debug())
  }
}
