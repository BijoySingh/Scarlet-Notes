package com.bijoysingh.quicknote.utils

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bijoysingh.quicknote.BuildConfig
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.google.gson.Gson

const val REMOTE_CONFIG_URL = "https://material-notes-63563.firebaseapp.com/config/config.txt"
const val REMOTE_CONFIG_REFETCH_TIME_MS = 7 * 24 * 60 * 60 * 1000
const val KEY_REMOTE_CONFIG_FETCH_TIME = "KEY_REMOTE_CONFIG_FETCH_TIME"

const val KEY_RC_LITE_VERSION = "KEY_RC_LITE_VERSION"
const val KEY_RC_FULL_VERSION = "KEY_RC_FULL_VERSION"

class RemoteConfig(
    val rc_lite_production_version: Int?,
    val rc_full_production_version: Int?)

class RemoteConfigFetcher(val context: Context) {
  fun init() {
    val lastFetched = userPreferences().get(KEY_REMOTE_CONFIG_FETCH_TIME, 0)
    if (System.currentTimeMillis() > lastFetched + REMOTE_CONFIG_REFETCH_TIME_MS) {
      fetchConfig()
    }
  }

  fun fetchConfig() {
    val request = object : StringRequest(
        Request.Method.GET,
        REMOTE_CONFIG_URL,
        Response.Listener { response -> onSuccess(response) },
        Response.ErrorListener { _ -> }) {}
    request.retryPolicy = DefaultRetryPolicy(
        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    request.setShouldCache(false)
    Volley.newRequestQueue(context).add(request)
  }

  fun onSuccess(response: String?) {
    if (response === null) {
      return
    }

    userPreferences().put(KEY_REMOTE_CONFIG_FETCH_TIME, System.currentTimeMillis())
    try {
      val config = Gson().fromJson<RemoteConfig>(response, RemoteConfig::class.java)
      userPreferences().put(KEY_RC_LITE_VERSION, config.rc_lite_production_version ?: 0)
      userPreferences().put(KEY_RC_FULL_VERSION, config.rc_full_production_version ?: 0)
    } catch (exception: Exception) {
      return
    }
  }

  companion object {
    fun isLatestAppVersion(): Boolean {
      val latestVersion =
          if (getAppFlavor() == Flavor.PRO) userPreferences().get(KEY_RC_FULL_VERSION, 0)
          else userPreferences().get(KEY_RC_LITE_VERSION, 0)
      return BuildConfig.VERSION_CODE >= latestVersion
    }
  }
}