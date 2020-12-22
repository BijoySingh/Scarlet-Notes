package com.maubis.scarlet.base.security.controller

import android.os.SystemClock
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.settings.sheet.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sheet.sSecurityAskPinAlways
import com.maubis.scarlet.base.settings.sheet.sSecurityCode

object PinLockController {
  private var sLastLoginTimeMs = 0L

  fun isPinCodeEnabled(): Boolean {
    return !TextUtils.isNullOrEmpty(sSecurityCode)
  }

  fun needsAppLock(): Boolean {
    // App lock enabled
    if (isPinCodeEnabled() && sSecurityAppLockEnabled) {
      return needsLockCheckImpl()
    }
    return false
  }

  fun needsLockCheck(): Boolean {
    if (sSecurityAskPinAlways) {
      return true
    }

    return needsLockCheckImpl()
  }

  private fun needsLockCheckImpl(): Boolean {
    val deltaSinceLastUnlock = SystemClock.uptimeMillis() - sLastLoginTimeMs

    // unlock stays 10 minutes
    if (sLastLoginTimeMs == 0L || deltaSinceLastUnlock > 1000 * 60 * 5) {
      return true
    }

    // reset lock time
    notifyPinVerified()
    return false
  }

  fun notifyPinVerified() {
    sLastLoginTimeMs = SystemClock.uptimeMillis()
  }

}