package com.maubis.scarlet.base.config.auth

import android.content.Context

interface IAuthenticator {

  fun setup(context: Context)

  fun isLoggedIn(): Boolean

  fun userId(): String?

  fun openLoginActivity(context: Context): Runnable?

  fun openForgetMeActivity(context: Context): Runnable?

  fun logout()
}