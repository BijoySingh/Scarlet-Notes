package com.bijoysingh.quicknote.firebase.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.firebase.activity.DataPolicyActivity.Companion.hasAcceptedThePolicy
import com.bijoysingh.quicknote.firebase.initFirebaseDatabase
import com.bijoysingh.quicknote.scarlet.sFirebaseKilled
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.main.recycler.KEY_FORCE_SHOW_SIGN_IN
import com.maubis.scarlet.base.support.ui.ThemedActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseRemovalActivity : ThemedActivity() {

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
  }

  private fun setButton(state: Boolean) {
    loggingIn.set(state)
    component = FirebaseRemovalRootView.create(componentContext)
        .onClick {
          // TODO: Delete remote data...
          setButton(true)
          GlobalScope.launch {
            instance.authenticator().logout()
            setButton(false)

            sFirebaseKilled = true
            instance.authenticator().openLoginActivity(context)?.run()
            finish()
          }
        }
        .removingItems(state)
        .build()
    setContentView(LithoView.create(componentContext, component))
  }

  override fun onBackPressed() {
    super.onBackPressed()
  }

  override fun notifyThemeChange() {
    setSystemTheme()
  }
}
