package com.bijoysingh.quicknote.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.utils.bind
import com.bijoysingh.quicknote.utils.firebaseUserId
import com.bijoysingh.quicknote.utils.logoutUser
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class ForgetMeActivity : ThemedActivity() {

  private val RC_SIGN_IN = 31245

  val forgetMeBtn: TextView by bind(R.id.btn_forget_me)
  val cancelBtn: TextView by bind(R.id.btn_cancel)
  lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forget_me)
    forgettingInProcess = true

    setupGoogleLogin()

    forgetMeBtn.setOnClickListener {
      val userId = firebaseUserId()
      if (userId === null) {
        return@setOnClickListener
      }

      forgettingInProcess = true
      MaterialNotes.firebaseNote?.removeValue { _, _ -> }
      MaterialNotes.firebaseTag?.removeValue { _, _ -> }

      FirebaseAuth.getInstance().currentUser
          ?.delete()
          ?.addOnCompleteListener {
            if (it.isSuccessful) {
              logoutUser()
              finish()
              return@addOnCompleteListener
            }

            reauthAndDelete()
          }
    }
    cancelBtn.setOnClickListener {
      finish()
    }
  }

  override fun notifyThemeChange() {

  }

  companion object {
    var forgettingInProcess = false
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

  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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
      // Ignore this, handled by following content
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

          user.delete()
              .addOnCompleteListener {
                if (!it.isSuccessful) {
                  ToastHelper.show(this, "Deletion of account failed")
                  return@addOnCompleteListener
                }

                logoutUser()
                finish()
              }
        }
  }


}
