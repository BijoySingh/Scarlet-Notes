package com.maubis.scarlet.base.config

import android.content.Context

class NullRemoteConfigFetcher : IRemoteConfigFetcher {

  override fun isLatestVersion(): Boolean = true

  override fun setup(context: Context) {

  }

}