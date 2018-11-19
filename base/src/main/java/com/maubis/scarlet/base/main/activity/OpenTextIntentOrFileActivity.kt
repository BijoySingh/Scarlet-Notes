package com.maubis.scarlet.base.main.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.TextUtils
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.export.support.NoteImporter
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.support.utils.bind
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import java.io.InputStreamReader

const val KEEP_PACKAGE = "com.google.android.keep"

class OpenTextIntentOrFileActivity : ThemedActivity() {

  lateinit var context: Context

  var filenameText: String = ""
  var titleText: String = ""
  var contentText: String = ""

  val filename: TextView by bind(R.id.filename)
  val title: TextView by bind(R.id.title)
  val content: TextView by bind(R.id.description)
  val backButton: ImageView by bind(R.id.back_button)
  val actionDone: UITextView by bind(R.id.import_or_edit_to_app)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_external_intent)

    context = this

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

  override fun onResume() {
    super.onResume()
    CoreConfig.instance.startListener(this)
  }

  private fun setView() {
    backButton.setOnClickListener { onBackPressed() }
    actionDone.setOnClickListener {
      MultiAsyncTask.execute(object : MultiAsyncTask.Task<Note> {
        override fun handle(result: Note?) {
          if (result !== null) {
            startActivity(ViewAdvancedNoteActivity.getIntent(context, result))
          }
          finish()
        }

        override fun run(): Note {
          val note = NoteBuilder().gen(titleText, contentText)
          note.save(context)
          return note
        }
      })
    }
  }

  fun handleIntent(): Boolean {
    val hasSendIntent = handleSendText(intent)
    if (hasSendIntent) {
      val note = when (isCallerKeep()) {
        true -> NoteBuilder().gen(titleText, NoteBuilder().genFromKeep(contentText))
        false -> NoteBuilder().gen(titleText, contentText)
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
      contentText = NoteImporter().readFileInputStream(InputStreamReader(inputStream))
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
      callingPackage?.contains(KEEP_PACKAGE) ?: false -> true
      (intent?.`package` ?: "").contains(KEEP_PACKAGE) -> true
      else -> false
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme();

    val containerLayout = findViewById<View>(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    val toolbarIconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON);
    backButton.setColorFilter(toolbarIconColor)

    val textColor = CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT)
    filename.setTextColor(textColor)
    title.setTextColor(textColor)
    content.setTextColor(textColor)

    val actionColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    actionDone.setImageTint(actionColor)
    actionDone.setTextColor(actionColor)
  }
}
