package com.bijoysingh.quicknote

import android.app.Application
import com.github.ajalt.reprint.core.Reprint

class MaterialNotes: Application() {
  override fun onCreate() {
    super.onCreate()
    Reprint.initialize(this)
    Reprint.initialize(this)
    Reprint.initialize(this)
  }
}