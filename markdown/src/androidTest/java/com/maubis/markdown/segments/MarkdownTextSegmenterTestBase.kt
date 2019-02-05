package com.maubis.markdown.segments

import com.maubis.markdown.segmenter.InvalidSegment
import com.maubis.markdown.segmenter.MarkdownSegment
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.NormalMarkdownSegment
import org.junit.Assert

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
abstract class MarkdownTextSegmenterTestBase {
  protected fun assert(expectedSegments: List<MarkdownSegment>, processedSegments: List<MarkdownSegment>) {
    Assert.assertEquals(
        "Error: Lists must have same size\n" +
            "${toString(expectedSegments)} vs ${toString(processedSegments)}",
        expectedSegments.size, processedSegments.size)
    expectedSegments.forEachIndexed { index, markdownSegment ->
      val processedSegment = processedSegments.get(index)
      Assert.assertEquals(
          "Error: Types are the same\n" +
              "${toString(expectedSegments)} vs ${toString(processedSegments)}",
          markdownSegment.type(), processedSegment.type())
      Assert.assertEquals(
          "Error: Text must be the same\n" +
              "${toString(expectedSegments)} vs ${toString(processedSegments)}",
          markdownSegment.text(), processedSegment.text())
      Assert.assertNotEquals(
          "Error: Type should never be INVALID\n" +
              "${toString(expectedSegments)} vs ${toString(processedSegments)}",
          MarkdownSegmentType.INVALID, processedSegment.type())
    }
  }

  protected fun assert(text: String, processedSegments: List<MarkdownSegment>) {
    val string = StringBuilder()
    processedSegments.forEachIndexed { index, markdownSegment ->
      string.append(markdownSegment.text())
      if (index != processedSegments.size - 1) {
        string.append("\n")
      }
    }

    Assert.assertEquals(text, string.toString())
  }

  protected fun getTestSegment(type: MarkdownSegmentType, text: String): MarkdownSegment {
    return NormalMarkdownSegment(InvalidSegment(type), text)
  }

  private fun toString(segments: List<MarkdownSegment>): String {
    val builder = StringBuilder()
    builder.append("{")
    segments.forEach {
      builder.append("(")
      builder.append(it.type().name)
      builder.append(",")
      builder.append(it.text())
      builder.append(")")
    }
    builder.append("}")
    return builder.toString()
  }
}
