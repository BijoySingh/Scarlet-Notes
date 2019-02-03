package com.maubis.markdown.inliners

import android.support.test.runner.AndroidJUnit4
import com.maubis.markdown.inliner.DefaultMarkdownInline
import com.maubis.markdown.inliner.NormalInlineSegment
import com.maubis.markdown.inliner.TextInliner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineBlankTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = TextInliner(text).get()
    assert(DefaultMarkdownInline(listOf()), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "\n\n"
    val processed = TextInliner(text).get()
    assert(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = TextInliner(text).get()
    assert(NormalInlineSegment(text), processed)
  }
}
