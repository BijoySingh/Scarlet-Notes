package com.bijoysingh.quicknote.firebase.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.firebase.activity.DataPolicyActivity.Companion.hasAcceptedThePolicy
import com.bijoysingh.quicknote.firebase.initFirebaseDatabase
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
import com.maubis.scarlet.base.main.recycler.KEY_FORCE_SHOW_SIGN_IN
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.maybeThrow
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseLoginActivity : ThemedActivity() {

  private val RC_SIGN_IN = 31245

  lateinit var context: Context
  lateinit var googleSignInClient: GoogleSignInClient
  lateinit var firebaseAuth: FirebaseAuth

  lateinit var component: Component
  lateinit var componentContext: ComponentContext

  var loggingIn = AtomicBoolean(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    context = this
    componentContext = ComponentContext(context)
    setButton(false)
    setupGoogleLogin()
    firebaseAuth = FirebaseAuth.getInstance()
    notifyThemeChange()
  }

  private fun setButton(state: Boolean) {
    loggingIn.set(state)
    component = FirebaseRootView.create(componentContext)
        .onClick {
          if (!hasAcceptedThePolicy()) {
            IntentUtils.startActivity(this, DataPolicyActivity::class.java)
            return@onClick
          }
          if (!loggingIn.get()) {
            setButton(true)
            signIn()
          }
        }
        .loggingIn(state)
        .build()
    setContentView(LithoView.create(componentContext, component))
  }

  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso)
  }

  private fun signIn() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }

  override fun onBackPressed() {
    if (!loggingIn.get()) {
      super.onBackPressed()
    }
  }

  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      handleSignInResult(task)
    }
  }

  private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
    try {
      val account = task.getResult(ApiException::class.java)
      if (account !== null) {
        firebaseAuthWithGoogle(account)
        return
      }
    } catch (exception: Exception) {
      maybeThrow(this, exception)
    }
    onLoginFailure()
  }

  private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
          override fun onComplete(task: Task<AuthResult>) {
            if (task.isSuccessful()) {
              val user = firebaseAuth.currentUser
              onLoginSuccess(user)
            } else {
              Log.e("Firebase", "Failed")
              onLoginFailure()
            }
          }
        })
  }

  private fun onLoginSuccess(user: FirebaseUser?) {
    if (user === null || user.uid.isEmpty()) {
      return
    }

    ApplicationBase.instance.store().put(KEY_FORCE_SHOW_SIGN_IN, true)
    setButton(false)
    initFirebaseDatabase(context, user.uid)
    finish()
  }

  private fun onLoginFailure() {
    ToastHelper.show(context, R.string.login_to_google_failed)
    setButton(false)
  }

  override fun notifyThemeChange() {
    setSystemTheme()
  }
}
