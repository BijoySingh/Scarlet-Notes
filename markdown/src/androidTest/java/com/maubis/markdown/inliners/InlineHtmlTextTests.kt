package com.maubis.markdown.inliners

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineHtmlTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testEmptyText() {
    val text = "Hi<b>Hello<i>World</i></b>"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf(
        NormalInlineMarkdownSegment("Hi"),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD), listOf(NormalInlineMarkdownSegment("Hello"),
            PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.ITALICS), listOf(NormalInlineMarkdownSegment("World"))))))), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "<u>Hello</u><code>World</code><em>Italics</em><strong>Strong</strong>"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf(
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.UNDERLINE), listOf(NormalInlineMarkdownSegment("Hello"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE), listOf(NormalInlineMarkdownSegment("World"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.ITALICS), listOf(NormalInlineMarkdownSegment("Italics"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD), listOf(NormalInlineMarkdownSegment("Strong"))))), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = TextInliner(text).get()
    assert(NormalInlineMarkdownSegment(text), processed)
  }
}
