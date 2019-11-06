package com.maubis.markdown.segments

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SegmentSimpleMultiLineTests : MarkdownTextSegmenterTestBase() {
  @Test
  fun testMultiLineText() {
    val text = "text\n" +
        " text\n" +
        "\n" +
        "text"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.NORMAL, "text\n text\n\ntext")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testMultipleHeadlines() {
    val text = "# text\n" +
        "## text\n" +
        "### text\n" +
        "text\n"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.HEADING_1, "# text"),
        getTestSegment(MarkdownSegmentType.HEADING_2, "## text"),
        getTestSegment(MarkdownSegmentType.HEADING_3, "### text"),
        getTestSegment(MarkdownSegmentType.NORMAL, "text\n")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testMultipleBullets() {
    val text = "- text\n" +
        "- text\n" +
        "  - text\n" +
        "    - text\n" +
        "text\n"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.BULLET_1, "- text"),
        getTestSegment(MarkdownSegmentType.BULLET_1, "- text"),
        getTestSegment(MarkdownSegmentType.BULLET_2, "  - text"),
        getTestSegment(MarkdownSegmentType.BULLET_3, "    - text"),
        getTestSegment(MarkdownSegmentType.NORMAL, "text\n")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testMultipleQuotes() {
    val text = "> text\n" +
        "text\n" +
        "> text\n\n" +
        "> text"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.QUOTE, "> text\ntext"),
        getTestSegment(MarkdownSegmentType.QUOTE, "> text"),
        getTestSegment(MarkdownSegmentType.NORMAL, ""),
        getTestSegment(MarkdownSegmentType.QUOTE, "> text")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testCodeAndQuotes() {
    val text = "## text\n\n" +
        "```\ncode\n```\n\n" +
        "> text\n\n" +
        "- bullet"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.HEADING_2, "## text"),
        getTestSegment(MarkdownSegmentType.NORMAL, ""),
        getTestSegment(MarkdownSegmentType.CODE, "```\ncode\n```"),
        getTestSegment(MarkdownSegmentType.NORMAL, ""),
        getTestSegment(MarkdownSegmentType.QUOTE, "> text"),
        getTestSegment(MarkdownSegmentType.NORMAL, ""),
        getTestSegment(MarkdownSegmentType.BULLET_1, "- bullet")),
        processed)
    assert(text, processed)
  }

  @Test
  fun testMultilineCode() {
    val text = "```\n" +
        "text\n" +
        "# text\n" +
        "## text\n" +
        "### text\n" +
        "> text\n" +
        "\n" +
        "```"
    val processed = TextSegmenter(text).get()
    assert(listOf(getTestSegment(MarkdownSegmentType.CODE, text)), processed)
    assert(text, processed)
  }

  @Test
  fun testSeparatorCode() {
    val text = "```\n" +
        "code\n" +
        "```\n" +
        "---\n" +
        "## heading"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.CODE, "```\ncode\n```"),
        getTestSegment(MarkdownSegmentType.SEPARATOR, "---"),
        getTestSegment(MarkdownSegmentType.HEADING_2, "## heading")), processed)
    assert(text, processed)
  }



  @Test
  fun testMarkdownInsideCode() {
    val text = "```\n" +
        "**co** _d_ \\e\n" +
        "```\n" +
        "---\n" +
        "## heading"
    val processed = TextSegmenter(text).get()
    assert(listOf(
        getTestSegment(MarkdownSegmentType.CODE, "```\n**co** _d_ \\e\n```"),
        getTestSegment(MarkdownSegmentType.SEPARATOR, "---"),
        getTestSegment(MarkdownSegmentType.HEADING_2, "## heading")), processed)
    assert(text, processed)
  }

}
