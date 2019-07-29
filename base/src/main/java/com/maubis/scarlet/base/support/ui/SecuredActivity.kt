package com.maubis.scarlet.base.support.ui

import android.content.Intent
import com.maubis.scarlet.base.security.activity.AppLockActivity
import com.maubis.scarlet.base.security.controller.AppLockController

abstract class SecuredActivity : ThemedActivity() {
  override fun onResume() {
    super.onResume()
    if (AppLockController.needsAppLock()) {
      startActivity(Intent(this, AppLockActivity::class.java))
    }
  }
}