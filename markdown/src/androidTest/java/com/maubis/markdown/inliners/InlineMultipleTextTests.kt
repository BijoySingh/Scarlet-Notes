package com.maubis.markdown.inliners

import android.support.test.runner.AndroidJUnit4
import com.maubis.markdown.inliner.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineMultipleTextTests : MarkdownTextInlinerTestBase() {
  @Test
  fun testSimpleText() {
    val text = "t1 **t2** *t3* _t4_ ~t5~ `t6`"
    val processed = TextInliner(text).get()
    assert(DefaultMarkdownInline(listOf(
        NormalInlineSegment("t1 "),
        BoldMarkdownInline(listOf(NormalInlineSegment("t2"))),
        NormalInlineSegment(" "),
        UnderlineMarkdownInline(listOf(NormalInlineSegment("t3"))),
        NormalInlineSegment(" "),
        ItalicsMarkdownInline(listOf(NormalInlineSegment("t4"))),
        NormalInlineSegment(" "),
        StrikeMarkdownInline(listOf(NormalInlineSegment("t5"))),
        NormalInlineSegment(" "),
        CodeMarkdownInline(listOf(NormalInlineSegment("t6"))))), processed)
  }


  @Test
  fun testStickingTogether() {
    val text = "t1**t2***t3*_t4_~t5~`t6`"
    val processed = TextInliner(text).get()
    assert(DefaultMarkdownInline(listOf(
        NormalInlineSegment("t1"),
        BoldMarkdownInline(listOf(NormalInlineSegment("t2"))),
        UnderlineMarkdownInline(listOf(NormalInlineSegment("t3"))),
        ItalicsMarkdownInline(listOf(NormalInlineSegment("t4"))),
        StrikeMarkdownInline(listOf(NormalInlineSegment("t5"))),
        CodeMarkdownInline(listOf(NormalInlineSegment("t6"))))), processed)
  }
}
