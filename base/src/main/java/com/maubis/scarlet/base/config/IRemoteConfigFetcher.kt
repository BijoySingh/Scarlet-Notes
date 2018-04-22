package com.maubis.scarlet.base.config

import android.content.Context

interface IRemoteConfigFetcher {
  fun setup(context: Context)

  fun isLatestVersion(): Boolean
}