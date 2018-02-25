package com.bijoysingh.quicknote.utils

import com.bijoysingh.quicknote.BuildConfig

enum class Flavor {
  NONE, // FDroid, Master Builds
  LITE, // Play Store Version
  PRO, // Play Store Pro Version
}

fun getAppFlavor(): Flavor {
  return when (BuildConfig.FLAVOR) {
    "lite" -> Flavor.LITE
    "full" -> Flavor.PRO
    else -> Flavor.NONE
  }
}

fun isLoggedIn(): Boolean {
  return false
}