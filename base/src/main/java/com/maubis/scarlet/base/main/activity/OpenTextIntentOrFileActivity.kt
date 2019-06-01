package com.maubis.scarlet.base.main.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.TextUtils
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.markdown.Markdown
import com.maubis.markdown.spannable.clearMarkdownSpans
import com.maubis.markdown.spannable.setFormats
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.export.support.NoteImporter
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.bind
import java.io.InputStreamReader


const val KEEP_PACKAGE = "com.google.android.keep"
const val INTENT_KEY_DIRECT_NOTES_TRANSFER = "direct_notes_transfer"

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
    context = this
    val shouldHandleIntent = handleIntent()
    if (!shouldHandleIntent) {
      finish()
      return
    }

    setContentView(R.layout.activity_external_intent)
    setView()
    notifyThemeChange()


    val spannable = SpannableString(contentText)
    spannable.setFormats(Markdown.getSpanInfo(contentText).spans)
    content.setText(spannable, TextView.BufferType.SPANNABLE)

    title.setText(titleText)
    title.visibility = if (TextUtils.isNullOrEmpty(titleText)) View.GONE else View.VISIBLE
    filename.setText(filenameText)

    content.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

      }

      override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (text is Editable) {
          text.clearMarkdownSpans()
          text.setFormats(Markdown.getSpanInfo(text.toString()).spans)
        }
      }

      override fun afterTextChanged(text: Editable) {

      }

    })
  }

  override fun onResume() {
    super.onResume()
    ApplicationBase.instance.startListener(this)
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
    val hasDirectIntent = handleDirectSendText(intent)
    if (hasDirectIntent) {
      return false
    }

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

  fun handleDirectSendText(intent: Intent): Boolean {
    val sharedText = intent.getStringExtra(INTENT_KEY_DIRECT_NOTES_TRANSFER)
    if (sharedText === null || sharedText.isBlank()) {
      return false
    }
    NoteImporter().gen(this, sharedText)
    return true
  }

  fun handleFileIntent(intent: Intent): Boolean {
    val data = intent.data
    val lastPathSegment = data?.lastPathSegment
    if (data === null || lastPathSegment === null) {
      return false
    }

    try {
      val inputStream = contentResolver.openInputStream(data)
      contentText = NoteImporter().readFileInputStream(InputStreamReader(inputStream))
      filenameText = lastPathSegment
      inputStream?.close()
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

    val toolbarIconColor = ApplicationBase.instance.themeController().get(ThemeColorType.TOOLBAR_ICON);
    backButton.setColorFilter(toolbarIconColor)

    val textColor = ApplicationBase.instance.themeController().get(ThemeColorType.SECONDARY_TEXT)
    filename.setTextColor(textColor)
    title.setTextColor(textColor)
    content.setTextColor(textColor)

    val actionColor = ApplicationBase.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    actionDone.setImageTint(actionColor)
    actionDone.setTextColor(actionColor)
  }
}
