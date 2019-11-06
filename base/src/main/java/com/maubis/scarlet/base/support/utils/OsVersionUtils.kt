package com.maubis.scarlet.base.support.utils

import android.os.Build

object OsVersionUtils {
  fun biometricsManagerIsBetter() = Build.VERSION.SDK_INT >= 28

  fun canExtractReferrer() = Build.VERSION.SDK_INT >= 22

  fun requiresPermissions() = Build.VERSION.SDK_INT >= 23

  fun canSetStatusBarTheme() = Build.VERSION.SDK_INT >= 23

  fun canExtractActiveNotifications() = Build.VERSION.SDK_INT >= 23

  fun canAddLauncherShortcuts() = Build.VERSION.SDK_INT >= 26

  fun canAddNotificationChannels() = Build.VERSION.SDK_INT >= 26

  fun canUseSystemTheme() = Build.VERSION.SDK_INT >= 29
}
