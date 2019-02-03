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
    assert(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBoldText() {
    val text = "Hello World"
    val textToTest = "**$text**"
    val processed = TextInliner(textToTest).get()
    assert(BoldMarkdownInline(listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testItalicsText() {
    val text = "Hello World"
    val textToTest = "_${text}_"
    val processed = TextInliner(textToTest).get()
    assert(ItalicsMarkdownInline(listOf(NormalInlineSegment(text))), processed)
  }
  @Test
  fun testStrikeThroughText() {
    val text = "Hello World"
    val textToTest = "~$text~"
    val processed = TextInliner(textToTest).get()
    assert(StrikeMarkdownInline(listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testUnderlineText() {
    val text = "Hello World"
    val textToTest = "*$text*"
    val processed = TextInliner(textToTest).get()
    assert(UnderlineMarkdownInline(listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testCodeText() {
    val text = "Hello World"
    val textToTest = "`$text`"
    val processed = TextInliner(textToTest).get()
    assert(CodeMarkdownInline(listOf(NormalInlineSegment(text))), processed)
  }
}
