package com.bijoysingh.quicknote.drive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.support.KEY_EXTERNAL_FOLDER_SYNC_ENABLED
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import kotlinx.android.synthetic.main.gdrive_login.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


// TODO: This is not ready... Recent changes in Drive API make this sh*t a little difficult and
// inconclusive. I want to do this because it's safer than Firebase, but f*ck Google for
// changing the API So much

const val KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE = "g_drive_first_time_sync_note"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_TAG = "g_drive_first_time_sync_tag"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER = "g_drive_first_time_sync_folder"

var sGDriveFirstSyncNote: Boolean
  get() = CoreConfig.instance.store().get(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, false)
  set(value) = CoreConfig.instance.store().put(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, value)
var sGDriveFirstSyncTag: Boolean
  get() = CoreConfig.instance.store().get(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, false)
  set(value) = CoreConfig.instance.store().put(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, value)
var sGDriveFirstSyncFolder: Boolean
  get() = CoreConfig.instance.store().get(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, false)
  set(value) = CoreConfig.instance.store().put(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, value)

class GDriveLoginActivity : ThemedActivity(), GoogleApiClient.OnConnectionFailedListener {

  private val RC_SIGN_IN = 31244
  private val RC_SIGN_IN_PERMISSIONS = 32443

  lateinit var context: Context
  lateinit var mGoogleApiClient: GoogleApiClient

  var loggingIn = AtomicBoolean(false)
  var mDriveServiceHelper: GDriveServiceHelper? = null

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

  private fun signIn() {
    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
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
      try {
        val account = task.getResult(ApiException::class.java)
        if (account !== null) {
          onLoginComplete(account)
          return
        }
      } catch (exception: Exception) {
        // Ignore this, handled by following content
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

  override fun notifyThemeChange() {
    setSystemTheme()
    containerLayout.setBackgroundColor(getThemeColor())
    signInToGDrive.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    signInToGDriveDetails.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
  }

  fun onLoginComplete(account: GoogleSignInAccount) {
    mDriveServiceHelper = getDriveHelper(context, account)

    sGDriveFirstSyncNote = false
    sGDriveFirstSyncFolder = false
    sGDriveFirstSyncTag = false

    gDrive?.reset()
    gDrive = GDriveRemoteDatabase(WeakReference(this.applicationContext))
    gDrive?.init(mDriveServiceHelper!!)

    setButton(false)
  }

  override fun onConnectionFailed(p0: ConnectionResult) {
    ToastHelper.show(this, R.string.google_drive_page_connection_failed)
  }

  companion object {
    fun getDriveHelper(context: Context, account: GoogleSignInAccount): GDriveServiceHelper {
      val credential = GoogleAccountCredential.usingOAuth2(
          context,
          Collections.singleton(DriveScopes.DRIVE_FILE))
      credential.selectedAccount = account.account
      val googleDriveService = Drive.Builder(
          AndroidHttp.newCompatibleTransport(),
          GsonFactory(),
          credential)
          .setApplicationName(context.getString(R.string.app_name))
          .build()
      return GDriveServiceHelper(googleDriveService)
    }
  }
}
