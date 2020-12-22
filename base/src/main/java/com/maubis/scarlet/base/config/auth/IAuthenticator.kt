package com.maubis.scarlet.base.config.auth

import android.content.Context
import com.maubis.scarlet.base.support.ui.ThemedActivity

interface IAuthenticator {

  fun setup(context: Context)

  fun isLoggedIn(context: Context): Boolean

  fun isLegacyLoggedIn(): Boolean

  fun userId(context: Context): String?

  fun openLoginActivity(context: Context): Runnable?

  fun openForgetMeActivity(context: Context): Runnable?

  fun openTransferDataActivity(context: Context): Runnable?

  fun openLogoutActivity(context: Context): Runnable?

  fun setPendingUploadListener(listener: IPendingUploadListener?)

  fun showPendingSync(activity: ThemedActivity)

  fun requestSync(forced: Boolean)

  fun logout()
}