package com.maubis.scarlet.base.support

import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import java.util.*

fun addShortcut(context: Context, shortcut: ShortcutInfo) {
  if (!OsVersionUtils.canAddLauncherShortcuts()) {
    return
  }

  val shortcutManager = context.getSystemService(ShortcutManager::class.java)
  if (shortcutManager === null) {
    return
  }

  shortcutManager.dynamicShortcuts = listOf(shortcut)
  if (shortcutManager.isRequestPinShortcutSupported) {
    val pinShortcutInfo = ShortcutInfo.Builder(context, shortcut.id).build()
    val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

    val successCallback = PendingIntent.getBroadcast(context, 0,
        pinnedShortcutCallbackIntent, 0)
    shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
  }
}