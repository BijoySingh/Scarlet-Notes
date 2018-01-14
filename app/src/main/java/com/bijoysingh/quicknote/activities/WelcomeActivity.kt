package com.bijoysingh.quicknote.activities

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.IntentUtils

const val KEY_WELCOME_SHOW = "KEY_WELCOME_SHOW"

class WelcomeActivity : ThemedActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_welcome)

    DataStore.get(this).put(KEY_WELCOME_SHOW, true)
    findViewById<View>(R.id.get_started).setOnClickListener {
      IntentUtils.startActivity(this, MainActivity::class.java)
      finish()
    }
    setStatusBarColor(ContextCompat.getColor(this, R.color.welcome_page_bg))
    setStatusBarTextColor(true)
  }

  override fun notifyNightModeChange() {
    // Ignore for this activity

  }
}
