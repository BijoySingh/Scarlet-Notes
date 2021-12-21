package com.bijoysingh.quicknote.firebase.activity

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.scarlet.sFirebaseKilled
import com.bijoysingh.quicknote.scarlet.sGDriveLoggedIn
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.bind
import com.maubis.scarlet.base.support.utils.maybeThrow

class ForgetMeActivity : ThemedActivity() {

  private val RC_SIGN_IN = 31245

  val acceptCheckBox: CheckBox by bind(R.id.accept_policy)
  val forgetMeBtn: TextView by bind(R.id.btn_forget_me)
  val cancelBtn: TextView by bind(R.id.btn_cancel)

  lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forget_me)
    forgettingInProcess = true

    setupGoogleLogin()

    acceptCheckBox.setOnCheckedChangeListener { _, checked ->
      val textColor = if (checked) R.color.light_primary_text else R.color.dark_tertiary_text
      forgetMeBtn.setTextColor(ContextCompat.getColor(this, textColor))

      val backgroundColor = if (checked) R.color.colorAccent else R.color.transparent
      forgetMeBtn.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }
    forgetMeBtn.setOnClickListener {
      if (!acceptCheckBox.isChecked) {
        return@setOnClickListener
      }

      val isLoggedIn = ApplicationBase.instance.authenticator().isLegacyLoggedIn()
      if (!isLoggedIn) {
        return@setOnClickListener
      }

      forgettingInProcess = true
      firebaseForgetMe(
        onComplete = {
          sFirebaseKilled = true
          finish()
        },
        onFailure = {
          reauthAndDelete()
        })

    }
    cancelBtn.setOnClickListener {
      finish()
    }
  }

  override fun notifyThemeChange() {

  }

  companion object {
    var forgettingInProcess = false

    fun firebaseForgetMe(onComplete: () -> Unit = {}, onFailure: () -> Unit = {}) {
      firebase?.deleteEverything()

      FirebaseAuth.getInstance().currentUser
        ?.delete()
        ?.addOnCompleteListener {
          if (it.isSuccessful) {
            firebase?.logout()
            gDrive?.logout()
            sFirebaseKilled = true
            sGDriveLoggedIn = false
            onComplete()
            return@addOnCompleteListener
          }
          onFailure()
        }
    }
  }

  /**
   * Google Login for reauth
   */
  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso);
  }

  private fun reauthAndDelete() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
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
    ToastHelper.show(this, R.string.login_to_google_failed)
  }

  private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

    val user = FirebaseAuth.getInstance().currentUser
    user?.reauthenticate(credential)
      ?.addOnCompleteListener {
        if (!it.isSuccessful) {
          ToastHelper.show(this, "Reauthentication of account failed.")
          return@addOnCompleteListener
        }
        deleteUser(user)
      }
  }

  protected fun deleteUser(user: FirebaseUser) {
    user.delete()
      .addOnCompleteListener {
        if (!it.isSuccessful) {
          ToastHelper.show(this, "Deletion of account failed")
          return@addOnCompleteListener
        }

        ApplicationBase.instance.authenticator().logout()
        finish()
      }
  }

}
