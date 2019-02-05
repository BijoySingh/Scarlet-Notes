package com.maubis.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.inliner.TextInliner
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.markdown.spannable.SpanInfo
import com.maubis.markdown.spannable.map

object Markdown {
  fun get(text: String, strip: Boolean = false): Spannable {
    val segments = TextSegmenter(text).get()

    val spannable = SpannableStringBuilder()
    segments.forEach {
      // spannable.append(it.spannable(strip))
      spannable.append("\n")
    }
    return spannable
  }

  fun getSpanInfo(text: String): List<SpanInfo> {
    val segments = TextSegmenter(text).get()
    var currentIndex = 0
    val formats = ArrayList<SpanInfo>()
    segments.forEach {
      val finalIndex = currentIndex + it.text().length
      formats.add(SpanInfo(map(it.type()), currentIndex, finalIndex))
      formats.addAll(TextInliner(it.text()).get().spans(currentIndex))
      currentIndex = finalIndex + 1
    }
    return formats
  }

  fun debug(text: String): String {
    val segments = TextSegmenter(text).get()
    val string = StringBuilder()
    segments.map { string.append(TextInliner(it.text()).get().debug()) }
    return string.toString()
  }
}