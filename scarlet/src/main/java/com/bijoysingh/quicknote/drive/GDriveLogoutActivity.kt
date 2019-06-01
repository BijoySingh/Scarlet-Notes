package com.bijoysingh.quicknote.drive

import android.content.Context
import android.os.Bundle
import com.bijoysingh.quicknote.R
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.support.ui.ThemedActivity
import java.util.concurrent.atomic.AtomicBoolean

class GDriveLogoutActivity : ThemedActivity(), GoogleApiClient.OnConnectionFailedListener {

  private val RC_SIGN_IN = 31246

  lateinit var context: Context
  lateinit var component: Component
  lateinit var componentContext: ComponentContext
  lateinit var mGoogleApiClient: GoogleApiClient

  var signingOut = AtomicBoolean(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    context = this
    componentContext = ComponentContext(context)
    setButton(false)
    setupGoogleLogin()
    notifyThemeChange()
  }

  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    mGoogleApiClient = GoogleApiClient
        .Builder(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build()
  }

  private fun signOut() {
    Auth.GoogleSignInApi.signOut(mGoogleApiClient).addStatusListener {
      if (it.isSuccess) {
        onSignOutComplete()
      }
    }
  }

  override fun onBackPressed() {
    if (!signingOut.get()) {
      super.onBackPressed()
    }
  }

  private fun setButton(state: Boolean) {
    signingOut.set(state)
    component = GDriveLogoutRootView.create(componentContext)
        .onClick {
          if (!signingOut.get()) {
            setButton(true)
            signOut()
          }
        }
        .loggingIn(state)
        .build()
    setContentView(LithoView.create(componentContext, component))
  }

  override fun notifyThemeChange() {
    setSystemTheme()
  }

  fun onSignOutComplete() {
    instance.authenticator().logout()
    finish()
  }

  override fun onConnectionFailed(p0: ConnectionResult) {
    ToastHelper.show(this, R.string.google_drive_page_connection_failed)
  }
}
