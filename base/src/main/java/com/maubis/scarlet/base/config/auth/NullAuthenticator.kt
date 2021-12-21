package com.maubis.scarlet.base.config.auth

import android.content.Context
import com.maubis.scarlet.base.support.ui.ThemedActivity

class NullAuthenticator : IAuthenticator {

  override fun openLoginActivity(context: Context): Runnable? = null

  override fun openForgetMeActivity(context: Context): Runnable? = null

  override fun openTransferDataActivity(context: Context): Runnable? = null

  override fun openLogoutActivity(context: Context): Runnable? = null

  override fun logout() {}

  override fun setup(context: Context) {}

  override fun userId(context: Context): String? = null

  override fun isLoggedIn(context: Context): Boolean = false

  override fun isLegacyLoggedIn(): Boolean = false

  override fun setPendingUploadListener(listener: IPendingUploadListener?) {}

  override fun requestSync(forced: Boolean) {}

  override fun showPendingSync(activity: ThemedActivity) {}
}