package com.maubis.scarlet.base.support.utils

import android.content.Context
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppFlavor
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

enum class Flavor {
  NONE, // FDroid, Master Builds
  LITE, // Play Store Version
  PRO, // Play Store Pro Version
}

object FlavorUtils {
  const val PRO_APP_PACKAGE_NAME = "com.bijoysingh.quicknote.pro"
  const val KEY_PRO_APP_INSTALLED = "pro_app_installed"
  fun hasProAppInstalled(context: Context): Boolean {
    val reference = WeakReference(context)
    GlobalScope.launch(Dispatchers.IO) {
      val found = try {
        reference.get()?.packageManager?.getPackageInfo(PRO_APP_PACKAGE_NAME, 0) != null
      } catch (e: Exception) {
        throwOrReturn(e, false)
      }
      sAppPreferences.put(KEY_PRO_APP_INSTALLED, found)
    }
    return sAppPreferences.get(KEY_PRO_APP_INSTALLED, false)
  }

  fun isPro() = sAppFlavor == Flavor.PRO
  fun isLite() = sAppFlavor == Flavor.LITE
  fun isPlayStore() = sAppFlavor != Flavor.NONE
  fun isOpenSource() = sAppFlavor == Flavor.NONE
}