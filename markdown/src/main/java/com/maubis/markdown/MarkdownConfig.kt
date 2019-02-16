package com.maubis.markdown

import com.maubis.markdown.inliner.TextInlineConfig
import com.maubis.markdown.segmenter.TextSegmentConfig
import com.maubis.markdown.spans.SpanConfig

class MarkdownConfig {
  var segmenterConfig = TextSegmentConfig(TextSegmentConfig.Builder())
  var inlinerConfig = TextInlineConfig(TextInlineConfig.Builder())
  var spanConfig = SpanConfig()

  companion object {
    var config = MarkdownConfig()
  }
}