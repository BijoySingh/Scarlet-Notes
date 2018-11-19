package com.maubis.scarlet.base.support.utils

enum class Flavor {
  NONE, // FDroid, Master Builds
  LITE, // Play Store Version
  PRO, // Play Store Pro Version
}

object FlavourUtils {
  fun hasProAppInstalled(): Boolean {
    // TODO: Implement this properly
    return false
  }
}