package com.bijoysingh.quicknote.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.external.noteDatabaseReference
import com.bijoysingh.quicknote.database.external.tagDatabaseReference
import com.bijoysingh.quicknote.database.utils.saveToSync
import com.bijoysingh.quicknote.utils.Flavor
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.getAppFlavor
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ThemedActivity() {

  private val RC_SIGN_IN = 31244

  lateinit var context: Context
  lateinit var googleSignInClient: GoogleSignInClient
  lateinit var firebaseAuth: FirebaseAuth

  lateinit var button: View
  lateinit var installPro: View
  lateinit var buttonTitle: TextView
  var loggingIn = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    context = this
    setupSignInButton()
    setupGoogleLogin()
    firebaseAuth = FirebaseAuth.getInstance()
    notifyThemeChange()
  }

  private fun setupSignInButton() {
    button = findViewById(R.id.sign_in_button)
    buttonTitle = button.findViewById(R.id.sign_in_button_title)
    button.setOnClickListener {
      if (loggingIn) {
        // do nothing
      } else {
        setButton(true)
        signIn()
      }
    }

    installPro = findViewById(R.id.install_pro)
    installPro.setOnClickListener {
      IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      finish()
    }

    val proInformationVisibility = if (getAppFlavor() == Flavor.PRO) View.GONE else View.VISIBLE
    installPro.visibility = proInformationVisibility
    val installProTitle = findViewById<View>(R.id.install_pro_details_title)
    installProTitle.visibility = proInformationVisibility
    val installProDescription = findViewById<View>(R.id.install_pro_details_description)
    installProDescription.visibility = proInformationVisibility
    val installProSeparator = findViewById<View>(R.id.install_pro_separator)
    installProSeparator.visibility = proInformationVisibility
  }

  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso);
  }

  private fun signIn() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }

  override fun onBackPressed() {
    if (!loggingIn) {
      super.onBackPressed()
    }
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

    ToastHelper.show(context, R.string.login_to_google_failed)
    setButton(false)
  }

  private fun setButton(state: Boolean) {
    loggingIn = state
    if (loggingIn) {
      button.setBackgroundResource(R.drawable.login_button_disabled)
      buttonTitle.setText(R.string.logging_into_app)
    } else {
      button.setBackgroundResource(R.drawable.login_button_active)
      buttonTitle.setText(R.string.login_with_google)
    }
  }

  private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
          override fun onComplete(task: Task<AuthResult>) {
            if (task.isSuccessful()) {
              val user = firebaseAuth.currentUser
              transitionNotesToServer(user)
              buttonTitle.setText(R.string.logged_into_app)
            } else {
              ToastHelper.show(context, R.string.login_to_google_failed)
              setButton(false)
            }
          }
        })
  }

  private fun transitionNotesToServer(user: FirebaseUser?) {
    if (user === null || user.uid.isEmpty()) {
      return
    }
    noteDatabaseReference(context, user.uid)
    for (note in Note.db().all) {
      note.saveToSync()
    }

    tagDatabaseReference(context, user.uid)
    for (tag in Tag.db().all) {
      tag.saveToSync()
    }
    finish()
  }

  override fun notifyThemeChange() {
    setSystemTheme();

    val containerLayout = findViewById<View>(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    val optionsTitle = findViewById<TextView>(R.id.sign_in_title)
    optionsTitle.setTextColor(getAppTheme().get(this, ThemeColorType.SECONDARY_TEXT))

    val textColor = getAppTheme().get(this, ThemeColorType.TERTIARY_TEXT)
    val installProDescription = findViewById<TextView>(R.id.install_pro_details_description)
    val cloudSyncDescription = findViewById<TextView>(R.id.cloud_sync_details_description)
    installProDescription.setTextColor(textColor)
    cloudSyncDescription.setTextColor(textColor)

    val installProTitle = findViewById<TextView>(R.id.install_pro_details_title)
    val cloudSyncTitle = findViewById<TextView>(R.id.cloud_sync_details_title)
    val titleTextColor = getAppTheme().get(this, ThemeColorType.SECTION_HEADER)
    installProTitle.setTextColor(titleTextColor)
    cloudSyncTitle.setTextColor(titleTextColor)
  }
}
