package com.maubis.scarlet.base.support.ui

import android.view.View

fun visibility(isVisible: Boolean): Int {
  return visibility(isVisible, true)
}

fun visibility(isVisible: Boolean, gone: Boolean): Int {
  return when {
    isVisible -> View.VISIBLE
    gone -> View.GONE
    else -> View.INVISIBLE
  }
}