package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.inliner.TextInliner
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.markdown.spannable.SpanInfo
import com.maubis.markdown.spannable.SpanResult
import com.maubis.markdown.spannable.map
import com.maubis.markdown.spannable.setFormats

object Markdown {
  fun render(text: String, strip: Boolean = false): Spannable {
    val spans = getSpanInfo(text, strip)
    val spannable = SpannableStringBuilder(spans.text)
    spannable.setFormats(spans.spans)
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

  fun toMarkwonableText(text: String): String {
    val source = TextInliner(text).get().toMarkwon()
    source.replace(Regex("(\\S)\n(\\S)"), "$1  \n$2")
    return source
  }

  fun getSpanInfo(text: String, stripDelimiter: Boolean = false): SpanResult {
    val segments = TextSegmenter(text).get()
    var currentIndex = 0
    val textBuilder = StringBuilder()
    val formats = ArrayList<SpanInfo>()
    segments.forEach {
      val inliner = TextInliner(it.text()).get()
      val strippedText = inliner.contentText(stripDelimiter)
      val finalIndex = currentIndex + strippedText.length

      formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
      if (it.type() != MarkdownSegmentType.CODE) {
        formats.addAll(inliner.allContentSpans(stripDelimiter, currentIndex))
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