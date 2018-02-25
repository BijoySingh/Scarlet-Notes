package com.bijoysingh.quicknote.utils

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View
import java.io.File

fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
  @Suppress("UNCHECKED_CAST")
  return unsafeLazy { findViewById(idRes) as T }
}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
  @Suppress("UNCHECKED_CAST")
  return unsafeLazy { findViewById(idRes) as T }
}

fun File.deleteIfExist(): Boolean {
  return when {
    exists() -> delete()
    else -> false
  }
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)