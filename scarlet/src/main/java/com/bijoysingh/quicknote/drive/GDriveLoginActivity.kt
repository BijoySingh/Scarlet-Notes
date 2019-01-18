package com.bijoysingh.quicknote.drive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveClient
import com.google.android.gms.drive.DriveResourceClient
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import kotlinx.android.synthetic.main.gdrive_login.*
import java.util.concurrent.atomic.AtomicBoolean

// TODO: This is not ready... Recent changes in Drive API make this sh*t a little difficult and
// inconclusive. I want to do this because it's safer than Firebase, but f*ck Google for
// changing the API So much
class GDriveLoginActivity : ThemedActivity(), GoogleApiClient.OnConnectionFailedListener {

  private val RC_SIGN_IN = 31244
  private val RC_SIGN_IN_PERMISSIONS = 32443

  lateinit var context: Context
  lateinit var mGoogleApiClient: GoogleApiClient

  var mDriveClient: DriveClient? = null
  var mDriveResourceClient: DriveResourceClient? = null
  var loggingIn = AtomicBoolean(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.gdrive_login)
    context = this
    setupSignInButton()
    setupGoogleLogin()
    notifyThemeChange()
  }

  private fun setupSignInButton() {
    signInButton.setOnClickListener {
      if (!loggingIn.get()) {
        setButton(true)
        signIn()
      }
    }
  }

  private fun setupGoogleLogin() {
    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestScopes(Drive.SCOPE_APPFOLDER)
        .requestEmail().build()

    mGoogleApiClient = GoogleApiClient
        .Builder(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build()
  }

  private fun signIn() {
    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }

  override fun onBackPressed() {
    if (!loggingIn.get()) {
      super.onBackPressed()
    }
  }

  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN || requestCode == RC_SIGN_IN_PERMISSIONS) {
      if (mDriveResourceClient !== null) {
        onLoginComplete(context, mDriveResourceClient!!)
      }
    }
  }

  private fun setButton(state: Boolean) {
    loggingIn.set(state)
    when (loggingIn.get()) {
      true -> {
        signInButton.setBackgroundResource(R.drawable.login_button_disabled)
        signInButtonTitle.setText(R.string.logging_into_app)
      }
      false -> {
        signInButton.setBackgroundResource(R.drawable.login_button_active)
        signInButtonTitle.setText(R.string.login_with_google)
      }
    }
  }

  private fun recheckPermissions(account: GoogleSignInAccount): Boolean {
    if (!GoogleSignIn.hasPermissions(account, Drive.SCOPE_APPFOLDER)) {
      GoogleSignIn.requestPermissions(this, RC_SIGN_IN_PERMISSIONS, account, Drive.SCOPE_APPFOLDER)
      return false
    }
    return true
  }

  override fun notifyThemeChange() {
    setSystemTheme()
    containerLayout.setBackgroundColor(getThemeColor())
    signInToGDrive.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    signInToGDriveDetails.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
  }

  fun onLoginComplete(
      context: Context,
      deviceResourceClient: DriveResourceClient) {
    val appFolderTask = deviceResourceClient.getAppFolder()
    appFolderTask.addOnSuccessListener { folder ->

    }.addOnFailureListener {

    }
  }

  override fun onConnectionFailed(p0: ConnectionResult) {
    ToastHelper.show(this, R.string.google_drive_page_connection_failed)
  }

}
