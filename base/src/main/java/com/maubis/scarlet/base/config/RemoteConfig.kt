package com.maubis.scarlet.base.config

import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.support.Flavor
import com.maubis.scarlet.base.support.getAppFlavor

class RemoteConfig(
    val rc_lite_production_version: Int?,
    val rc_full_production_version: Int?)

const val KEY_RC_LITE_VERSION = "KEY_RC_LITE_VERSION"
const val KEY_RC_FULL_VERSION = "KEY_RC_FULL_VERSION"

fun isLatestAppVersion(): Boolean {
  val latestVersion = when (getAppFlavor()) {
    Flavor.PRO -> CoreConfig.instance.store().get(KEY_RC_FULL_VERSION, 0)
    Flavor.LITE -> CoreConfig.instance.store().get(KEY_RC_LITE_VERSION, 0)
    else -> 0
  }
  return BuildConfig.VERSION_CODE >= latestVersion
}