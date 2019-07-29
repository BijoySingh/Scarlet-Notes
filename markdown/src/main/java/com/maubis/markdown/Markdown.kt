package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.inliner.TextInliner
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.markdown.spannable.*

object Markdown {
  fun render(text: String, strip: Boolean = false): Spannable {
    val spans = getSpanInfo(text, strip)
    val spannable = SpannableStringBuilder(spans.text)
    spannable.setFormats(spans.spans)
    return spannable
  }

  fun renderWithCustomFormatting(text: String, strip: Boolean = false, customSpanInfoAction: (Spannable, SpanInfo) -> Boolean): Spannable {
    val spans = getSpanInfo(text, strip)
    val spannable = SpannableStringBuilder(spans.text)
    spans.spans.forEach {
      if (!customSpanInfoAction(spannable, it)) {
        spannable.setDefaultFormats(it)
      }
    }
    return spannable
  }

  fun renderSegment(text: String, strip: Boolean = false): Spannable {
    val inliner = TextInliner(text).get()
    val strippedText = inliner.contentText(strip)
    val formats = inliner.allContentSpans(strip, 0)

    val spannable = SpannableStringBuilder(strippedText)
    spannable.setFormats(formats)
    return spannable
  }

  fun getSpanInfo(text: String, stripDelimiter: Boolean = false): SpanResult {
    val segments = TextSegmenter(text).get()
    var currentIndex = 0
    val textBuilder = StringBuilder()
    val formats = ArrayList<SpanInfo>()
    segments.forEach {
      val finalIndex: Int
      val strippedText: String
      when {
        it.type() == MarkdownSegmentType.CODE -> {
          strippedText = if (stripDelimiter) it.strip() else it.text()
          finalIndex = currentIndex + strippedText.length
          formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
        }
        else -> {
          val inliner = TextInliner(if (stripDelimiter) it.strip() else it.text()).get()
          strippedText = inliner.contentText(stripDelimiter)
          finalIndex = currentIndex + strippedText.length

          formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
          formats.addAll(inliner.allContentSpans(stripDelimiter, currentIndex))
        }
      }

      currentIndex = finalIndex + 1
      textBuilder.append(strippedText)
      textBuilder.append("\n")
    }
    return SpanResult(textBuilder.toString(), formats)
  }

  fun debug(text: String): String {
    val segments = TextSegmenter(text).get()
    val string = StringBuilder()
    segments.map { string.append(TextInliner(it.text()).get().debug()) }
    return string.toString()
  }
}