package com.maubis.scarlet.base.support.utils

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes

fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
  @Suppress("UNCHECKED_CAST")
  return unsafeLazy { findViewById(idRes) as T }
}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
  @Suppress("UNCHECKED_CAST")
  return unsafeLazy { findViewById(idRes) as T }
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)