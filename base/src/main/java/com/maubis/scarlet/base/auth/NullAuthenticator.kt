package com.maubis.scarlet.base.auth

import android.content.Context

class NullAuthenticator : IAuthenticator {
  override fun openLoginActivity(context: Context): Runnable? = null

  override fun openForgetMeActivity(context: Context): Runnable? = null

  override fun logout() {}

  override fun setup(context: Context) {}

  override fun userId(): String? = null

  override fun isLoggedIn(): Boolean = false

}