package com.maubis.markdown.inliners

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineMultipleTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testSimpleText() {
    val text = "t1 **t2** *t3* _t4_ ~t5~ `t6`"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf(
        NormalInlineMarkdownSegment("t1 "),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD), listOf(NormalInlineMarkdownSegment("t2"))),
        NormalInlineMarkdownSegment(" "),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.ITALICS), listOf(NormalInlineMarkdownSegment("t3"))),
        NormalInlineMarkdownSegment(" "),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.UNDERLINE), listOf(NormalInlineMarkdownSegment("t4"))),
        NormalInlineMarkdownSegment(" "),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE), listOf(NormalInlineMarkdownSegment("t5"))),
        NormalInlineMarkdownSegment(" "),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE), listOf(NormalInlineMarkdownSegment("t6"))))), processed)
  }


  @Test
  fun testStickingTogether() {
    val text = "t1**t2***t3*_t4_~t5~`t6`"
    val processed = TextInliner(text).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf(
        NormalInlineMarkdownSegment("t1"),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.BOLD), listOf(NormalInlineMarkdownSegment("t2"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.ITALICS), listOf(NormalInlineMarkdownSegment("t3"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.UNDERLINE), listOf(NormalInlineMarkdownSegment("t4"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.STRIKE), listOf(NormalInlineMarkdownSegment("t5"))),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INLINE_CODE), listOf(NormalInlineMarkdownSegment("t6"))))), processed)
  }

  @Test
  fun testEscapedText() {
    val textA = "aaa\\_bb_c"
    val processedA = TextInliner(textA).get()
    assert(PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.INVALID), listOf(
        NormalInlineMarkdownSegment("aaa"),
        PhraseDelimiterMarkdownInline(InvalidInline(MarkdownInlineType.IGNORE_CHAR), listOf(NormalInlineMarkdownSegment("_"))),
        NormalInlineMarkdownSegment("bb_c"))), processedA)
  }
}
