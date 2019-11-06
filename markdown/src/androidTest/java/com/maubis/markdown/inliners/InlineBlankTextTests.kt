package com.maubis.markdown.inliners

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineBlankTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf()), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "\n\n"
    val processed = TextInliner(text).get()
    assert(NormalInlineMarkdownSegment(text), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = TextInliner(text).get()
    assert(NormalInlineMarkdownSegment(text), processed)
  }
}
