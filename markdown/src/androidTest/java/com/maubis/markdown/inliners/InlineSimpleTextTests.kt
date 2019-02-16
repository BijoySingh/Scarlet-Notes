package com.maubis.markdown.inliners

import android.support.test.runner.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineSimpleTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testSimpleText() {
    val text = "Hello World"
    val processed = TextInliner(text).get()
    assert(NormalInlineMarkdownSegment(text), processed)
  }

  @Test
  fun testBoldText() {
    val text = "Hello World"
    val textToTest = "**$text**"
    val processed = TextInliner(textToTest).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD),listOf(NormalInlineMarkdownSegment(text))), processed)
  }

  @Test
  fun testItalicsText() {
    val text = "Hello World"
    val textToTest = "_${text}_"
    val processed = TextInliner(textToTest).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.UNDERLINE),listOf(NormalInlineMarkdownSegment(text))), processed)
  }
  @Test
  fun testStrikeThroughText() {
    val text = "Hello World"
    val textToTest = "~$text~"
    val processed = TextInliner(textToTest).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE),listOf(NormalInlineMarkdownSegment(text))), processed)
  }

  @Test
  fun testUnderlineText() {
    val text = "Hello World"
    val textToTest = "*$text*"
    val processed = TextInliner(textToTest).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.ITALICS),listOf(NormalInlineMarkdownSegment(text))), processed)
  }

  @Test
  fun testCodeText() {
    val text = "Hello World"
    val textToTest = "`$text`"
    val processed = TextInliner(textToTest).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE),listOf(NormalInlineMarkdownSegment(text))), processed)
  }
}
