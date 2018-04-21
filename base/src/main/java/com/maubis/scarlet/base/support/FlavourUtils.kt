package com.maubis.scarlet.base.support

import com.maubis.scarlet.base.BuildConfig

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
