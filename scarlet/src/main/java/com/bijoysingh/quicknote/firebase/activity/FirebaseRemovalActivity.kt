package com.bijoysingh.quicknote.firebase.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity.Companion.firebaseForgetMe
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity.Companion.forgettingInProcess
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.support.ui.ThemedActivity
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseRemovalActivity : ThemedActivity(), GoogleApiClient.OnConnectionFailedListener {

  lateinit var context: Context
  lateinit var component: Component
  lateinit var componentContext: ComponentContext

  var loggingIn = AtomicBoolean(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    context = this
    componentContext = ComponentContext(context)
    setButton(false)
    notifyThemeChange()
    setupGoogleLogin()
  }

  private fun setButton(state: Boolean) {
    loggingIn.set(state)
    component = FirebaseRemovalRootView.create(componentContext)
        .onClick {
          if (loggingIn.get()) {
            return@onClick
          }

          setButton(true)
          forgettingInProcess = true
          firebaseForgetMe(
              onComplete = {
                signOut {
                  forgettingInProcess = false
                  instance.authenticator().openLoginActivity(context)?.run()
                  finish()
                }
              },
              onFailure = {
                forgettingInProcess = false
                ToastHelper.show(context, "Failed logging you out. Try logging in again.")
                context.startActivity(Intent(context, FirebaseLoginActivity::class.java))
                finish()
              })
        }
        .removingItems(state)
        .build()
    setContentView(LithoView.create(componentContext, component))
  }

  lateinit var mGoogleApiClient: GoogleApiClient
  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    mGoogleApiClient = GoogleApiClient
        .Builder(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build()
  }

  private fun signOut(onSuccess: () -> Unit) {
    if (mGoogleApiClient.isConnecting) {
      Handler().postDelayed({
        signOut(onSuccess)
      }, 500)
      return
    }

    if (mGoogleApiClient.isConnected) {
      Auth.GoogleSignInApi.signOut(mGoogleApiClient).addStatusListener {
        onSuccess()
      }
      return
    }

    ToastHelper.show(context, "Failed logging you out properly. Try again later.")
  }

  override fun onConnectionFailed(p0: ConnectionResult) {
    ToastHelper.show(context, "Failed logging you out properly. Try again later.")
  }


  override fun onBackPressed() {
    super.onBackPressed()
  }

  override fun notifyThemeChange() {
    setSystemTheme()
  }
}
