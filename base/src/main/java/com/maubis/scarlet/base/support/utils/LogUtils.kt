package com.maubis.scarlet.base.support.utils

import android.util.Log
import com.maubis.scarlet.base.BuildConfig

fun log(message: String) {
  log("Scarlet", message)
}

fun log(tag: String, description: String) {
  if (BuildConfig.DEBUG) {
    Log.d(tag, description)
  }
}