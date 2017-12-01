package com.bijoysingh.quicknote.activities

import android.support.v7.app.AppCompatActivity

abstract class ThemedActivity : AppCompatActivity() {

  var isNightMode = false

  abstract fun notifyNightModeChange()

  fun toggleNightMode() {
    requestSetNightMode(!isNightMode)
  }

  fun requestSetNightMode(nightMode: Boolean) {
    isNightMode = nightMode
    notifyNightModeChange()
  }

  companion object {
    val KEY_NIGHT_THEME: String = "KEY_NIGHT_THEME"

    fun getKey() = KEY_NIGHT_THEME
  }
}