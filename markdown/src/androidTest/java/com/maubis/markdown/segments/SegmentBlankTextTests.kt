package com.maubis.markdown.segments

import android.support.test.runner.AndroidJUnit4
import com.maubis.markdown.segmenter.NormalSegment
import com.maubis.markdown.segmenter.TextSegmenter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SegmentBlankTextTests : MarkdownTextSegmenterTestBase() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = TextSegmenter(text).get()
    assert(listOf(NormalSegment(text)), processed)
    assert(text, processed)
  }

  @Test
  fun testBlankText() {
    val text = "     "
    val processed = TextSegmenter(text).get()
    assert(listOf(NormalSegment(text)), processed)
    assert(text, processed)
  }

  @Test
  fun testJustNewlinesText() {
    val text = "\n\n\n"
    val processed = TextSegmenter(text).get()
    assert(
        listOf(
            NormalSegment(""),
            NormalSegment(""),
            NormalSegment(""),
            NormalSegment("")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testJustNewlineAndBlanksText() {
    val text = "   \n  \n \n    "
    val processed = TextSegmenter(text).get()
    assert(
        listOf(NormalSegment(text)),
        processed)
    assert(text, processed)
  }
}
