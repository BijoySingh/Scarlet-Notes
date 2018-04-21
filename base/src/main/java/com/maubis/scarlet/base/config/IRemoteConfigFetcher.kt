package com.maubis.scarlet.base.config

import android.content.Context

interface IRemoteConfigFetcher {
  fun tryFetching(context: Context)
}