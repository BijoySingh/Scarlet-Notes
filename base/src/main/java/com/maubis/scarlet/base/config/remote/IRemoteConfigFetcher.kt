package com.maubis.scarlet.base.config.remote

import android.content.Context

interface IRemoteConfigFetcher {
  fun setup(context: Context)

  fun isLatestVersion(): Boolean
}