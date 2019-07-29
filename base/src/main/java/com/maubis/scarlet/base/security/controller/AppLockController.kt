package com.maubis.scarlet.base.security.controller

import android.os.SystemClock
import com.maubis.scarlet.base.settings.sheet.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
import com.maubis.scarlet.base.settings.sheet.sSecurityAppLockEnabled

object AppLockController {
  private var sLastLoginTimeMs = 0L

  fun needsAppLock(): Boolean {
    if (hasPinCodeEnabled() && sSecurityAppLockEnabled) {
      // App lock enabled
      val deltaSinceLastUnlock = SystemClock.uptimeMillis() - sLastLoginTimeMs

      // unlock stays 10 minutes
      if (sLastLoginTimeMs == 0L || deltaSinceLastUnlock > 1000 * 60 * 10) {
        return true
      }

      // reset lock time
      notifyAppLock()
      return false
    }
    return false
  }

  fun notifyAppLock() {
    sLastLoginTimeMs = SystemClock.uptimeMillis()
  }

}