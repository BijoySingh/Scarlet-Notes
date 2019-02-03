package com.maubis.markdown.segments

import android.support.test.runner.AndroidJUnit4
import com.maubis.markdown.segmenter.*
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
        NormalSegment("text\n text"),
        NormalSegment("\ntext")),
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
        Heading1Segment("# text"),
        Heading2Segment("## text"),
        Heading3Segment("### text"),
        NormalSegment("text"),
        NormalSegment("")),
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
        Bullet1Segment("- text"),
        Bullet1Segment("- text"),
        Bullet2Segment("  - text"),
        Bullet3Segment("    - text"),
        NormalSegment("text"),
        NormalSegment("")),
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
        QuoteSegment("> text\ntext"),
        QuoteSegment("> text"),
        NormalSegment(""),
        QuoteSegment("> text")),
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
    assert(listOf(CodeSegment(text)), processed)
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
    assert(listOf(CodeSegment("```\ncode\n```"), SeparatorSegment("---"), Heading2Segment("## heading")), processed)
    assert(text, processed)
  }

}
