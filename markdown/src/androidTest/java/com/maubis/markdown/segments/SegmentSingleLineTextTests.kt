package com.maubis.markdown.segments

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SegmentSingleLineTextTests : MarkdownTextSegmenterTestBase() {
  @Test
  fun testSingleLineText() {
    val text = "test string"
    val processed = TextSegmenter(text).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.NORMAL, text)), processed)
    assert(text, processed)
  }

  @Test
  fun testSingleLineHeadings() {
    val textH1 = "# heading 1"
    val processedH1 = TextSegmenter(textH1).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.HEADING_1, textH1)), processedH1)
    assert(textH1, processedH1)

    val textH2 = "## heading 2"
    val processedH2 = TextSegmenter(textH2).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.HEADING_2, textH2)), processedH2)
    assert(textH2, processedH2)

    val textH3 = "### heading 3"
    val processedH3 = TextSegmenter(textH3).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.HEADING_3, textH3)), processedH3)
    assert(textH3, processedH3)

    val textH4 = "#### normal"
    val processedH4 = TextSegmenter(textH4).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.NORMAL, textH4)), processedH4)
    assert(textH4, processedH4)
  }

  @Test
  fun testSingleLineBullets() {
    val textB1 = "- bullet"
    val processedB1 = TextSegmenter(textB1).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.BULLET_1, textB1)), processedB1)
    assert(textB1, processedB1)

    val textB2 = "  - bullet"
    val processedB2 = TextSegmenter(textB2).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.BULLET_2, textB2)), processedB2)
    assert(textB2, processedB2)

    val textB3 = "     - bullet"
    val processedB3 = TextSegmenter(textB3).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.BULLET_3, textB3)), processedB3)
    assert(textB3, processedB3)
  }

  @Test
  fun testSingleLineQuote() {
    val text = "> quote"
    val processed = TextSegmenter(text).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.QUOTE, text)), processed)
    assert(text, processed)
  }

  @Test
  fun testSingleLineCode() {
    val text = "```"
    val processed = TextSegmenter(text).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.CODE, text)), processed)
    assert(text, processed)
  }
}
