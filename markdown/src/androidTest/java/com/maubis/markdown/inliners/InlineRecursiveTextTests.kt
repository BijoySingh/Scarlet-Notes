package com.maubis.markdown.inliners

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class InlineRecursiveTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testSimple2Levels() {
    val text = "**~hello~**"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD),
        listOf(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE),
            listOf(NormalInlineMarkdownSegment("hello"))))), processed)
  }


  @Test
  fun testDeeperLevelComplexLevels() {
    val text = "**t1~t2~**"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD),
        listOf(NormalInlineMarkdownSegment("t1"),
            PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE),listOf(NormalInlineMarkdownSegment("t2"))))), processed)
  }

  @Test
  fun testCodeMultileveLevels() {
    val text = "`t1 **t2** *u*`"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE),listOf(NormalInlineMarkdownSegment("t1 **t2** *u*"))), processed)
  }

  @Test
  fun testMismatchingLevelsKind1() {
    val text1 = "**t~t**"
    val processed1 = TextInliner(text1).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD),listOf(NormalInlineMarkdownSegment("t~t"))), processed1)

    val text2 = "`t~t`"
    val processed2 = TextInliner(text2).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE),listOf(NormalInlineMarkdownSegment("t~t"))), processed2)


    val text3 = "~t*t~"
    val processed3 = TextInliner(text3).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE),listOf(NormalInlineMarkdownSegment("t*t"))), processed3)
  }

  @Test
  fun testMismatchingLevelsKind2() {
    val text2 = "~t~t~"
    val processed2 = TextInliner(text2).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID),listOf(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE),listOf(NormalInlineMarkdownSegment("t"))), NormalInlineMarkdownSegment("t~"))), processed2)
  }
}
