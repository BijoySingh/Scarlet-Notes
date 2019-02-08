package com.maubis.scarlet.base.support.utils

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import ru.noties.markwon.Markwon

fun getEditorActionListener(
    runnable: () -> Boolean,
    preConditions: () -> Boolean = { false }): TextView.OnEditorActionListener {
  return TextView.OnEditorActionListener { _: TextView, actionId: Int, event: KeyEvent? ->
    if (preConditions()) {
      return@OnEditorActionListener false
    }

    if (event == null) {
      if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
        return@OnEditorActionListener false
      }
    } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
      if (event.action != KeyEvent.ACTION_DOWN) {
        return@OnEditorActionListener true
      }
    } else {
      return@OnEditorActionListener false
    }
    return@OnEditorActionListener runnable()
  }
}

fun trim(source: CharSequence?): CharSequence {
  if (source == null || source.isEmpty()) {
    return ""
  }

  var index = source.length
  while (--index >= 0 && Character.isWhitespace(source[index])) {
    // Ignore, find the first non-whitespace character
  }
  return source.subSequence(0, index + 1)
}

fun markwonFix(source: String): String {
  return markwonNewlineFix(source)
}

/**
 * Replace a "X\nY" with "X  \nY"
 */
fun markwonNewlineFix(source: String): String {
  return source.replace(Regex("(\\S)\n(\\S)"), "$1  \n$2")
}

fun removeMarkdownHeaders(source: String): String {
  return source.replace(Regex("(^|\n)(\\s*)(#+)(\\s)"), "$1$2$4")
}

fun renderMarkdown(context: Context, source: String): CharSequence {
  val markdownText = markwonFix(source)
  return trim(Markwon.markdown(context, markdownText))
}