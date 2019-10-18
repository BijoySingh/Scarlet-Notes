package com.maubis.scarlet.base.support

import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import java.util.*

fun addShortcut(context: Context, shortcut: ShortcutInfo) {
  if (Build.VERSION.SDK_INT < 26) {
    return
  }

  val shortcutManager = context.getSystemService(ShortcutManager::class.java)
  shortcutManager.dynamicShortcuts = Arrays.asList(shortcut)

  if (shortcutManager.isRequestPinShortcutSupported) {
    val pinShortcutInfo = ShortcutInfo.Builder(context, shortcut.id).build()
    val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

    val successCallback = PendingIntent.getBroadcast(context, 0,
        pinnedShortcutCallbackIntent, 0)
    shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
  }

}