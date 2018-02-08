package com.bijoysingh.quicknote.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity.Companion.convertStreamToString
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.genEmptyNote
import com.bijoysingh.quicknote.utils.genImportFromKeep
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.TextUtils
import com.github.bijoysingh.uibasics.views.UITextView

const val KEEP_PACKAGE = "com.google.android.keep"
class ExternalIntentActivity : ThemedActivity() {

  lateinit var context: Context
  lateinit var store: DataStore

  var filenameText: String = ""
  var titleText: String = ""
  var contentText: String = ""

  lateinit var filename: TextView
  lateinit var title: TextView
  lateinit var content: TextView

  lateinit var backButton: ImageView
  lateinit var actionDone: UITextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_external_intent)

    context = this
    store = DataStore.get(context)

    setView()
    notifyThemeChange()
    val shouldHandleIntent = handleIntent()
    if (!shouldHandleIntent) {
      finish()
      return
    }

    content.setText(contentText)
    title.setText(titleText)
    title.visibility = if (TextUtils.isNullOrEmpty(titleText)) View.GONE else View.VISIBLE
    filename.setText(filenameText)
  }

  private fun setView() {
    filename = findViewById(R.id.filename)
    title = findViewById(R.id.title)
    content = findViewById(R.id.description)
    backButton = findViewById(R.id.back_button)
    actionDone = findViewById(R.id.import_or_edit_to_app)

    backButton.setOnClickListener { onBackPressed() }
    actionDone.setOnClickListener {
      val note = genEmptyNote(titleText, contentText)
      note.save(this)
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
      finish()
    }
  }

  fun handleIntent(): Boolean {
    val hasSendIntent = handleSendText(intent)
    if (hasSendIntent) {
      val note = when(isCallerKeep()) {
        true -> genEmptyNote(titleText, genImportFromKeep(contentText))
        false -> genEmptyNote(titleText, contentText)
      }
      note.save(this)
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
      return false
    }
    val hasFileIntent = handleFileIntent(intent)
    if (hasFileIntent) {
      return true
    }
    return false
  }

  fun handleSendText(intent: Intent): Boolean {
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    val sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

    titleText = sharedSubject ?: sharedTitle ?: ""
    contentText = sharedText ?: ""
    return sharedText != null
  }

  fun handleFileIntent(intent: Intent): Boolean {
    val data = intent.data
    try {
      val inputStream = contentResolver.openInputStream(data)
      contentText = convertStreamToString(inputStream)
      filenameText = data.lastPathSegment
      inputStream.close()
      return true
    } catch (exception: Exception) {
      return false
    }
  }

  fun isCallerKeep(): Boolean {
    return when {
      Build.VERSION.SDK_INT >= 22 && (referrer?.toString() ?: "").contains(KEEP_PACKAGE) -> true
      callingPackage.contains(KEEP_PACKAGE) -> true
      (intent?.`package` ?: "").contains(KEEP_PACKAGE) -> true
      else -> false
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme();

    val containerLayout = findViewById<View>(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    val toolbarIconColor = getAppTheme().get(context, ThemeColorType.TOOLBAR_ICON);
    backButton.setColorFilter(toolbarIconColor)

    val textColor = getAppTheme().get(this, ThemeColorType.SECONDARY_TEXT)
    filename.setTextColor(textColor)
    title.setTextColor(textColor)
    content.setTextColor(textColor)

    val actionColor = getAppTheme().get(this, ThemeColorType.TOOLBAR_ICON)
    actionDone.setImageTint(actionColor)
    actionDone.setTextColor(actionColor)
  }
}
