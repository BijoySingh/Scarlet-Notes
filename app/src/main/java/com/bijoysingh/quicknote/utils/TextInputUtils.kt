package com.bijoysingh.quicknote.utils

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

fun getEditorActionListener(runnable: () -> Boolean): TextView.OnEditorActionListener {
  return TextView.OnEditorActionListener { view: TextView, actionId: Int, event: KeyEvent? ->
    if (event == null) {
      if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
        return@OnEditorActionListener false
      }
    } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
      if (event.getAction() != KeyEvent.ACTION_DOWN) {
        return@OnEditorActionListener true
      }
    } else {
      return@OnEditorActionListener false
    }
    return@OnEditorActionListener runnable()
  }
}

fun trim(source: CharSequence?): CharSequence {
  if (source == null || source.length == 0) {
    return ""
  }

  var index = source.length
  while (--index >= 0 && Character.isWhitespace(source[index])) {
    // Ignore, find the first non-whitespace character
  }
  return source.subSequence(0, index + 1)
}