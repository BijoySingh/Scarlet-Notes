package com.maubis.scarlet.base.support.utils

import android.content.Context
import com.maubis.scarlet.base.config.CoreConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


enum class Flavor {
  NONE, // FDroid, Master Builds
  LITE, // Play Store Version
  PRO, // Play Store Pro Version
}

object FlavourUtils {
  const val PRO_APP_PACKAGE_NAME = "com.bijoysingh.quicknote.pro"
  const val KEY_PRO_APP_INSTALLED = "pro_app_installed"
  fun hasProAppInstalled(context: Context): Boolean {
    val reference = WeakReference(context)
    GlobalScope.launch(Dispatchers.IO) {
      var found = false
      try {
        found = reference.get()?.packageManager?.getPackageInfo(PRO_APP_PACKAGE_NAME, 0) != null
      } catch (e: Exception) {
        found = false
      }
      CoreConfig.instance.store().put(KEY_PRO_APP_INSTALLED, found)
    }
    return CoreConfig.instance.store().get(KEY_PRO_APP_INSTALLED, false)
  }
}