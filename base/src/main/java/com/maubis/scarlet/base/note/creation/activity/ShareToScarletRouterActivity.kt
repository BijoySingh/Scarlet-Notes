package com.maubis.scarlet.base.note.creation.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppImageStorage
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.export.support.NoteImporter
import com.maubis.scarlet.base.main.activity.INTENT_KEY_DIRECT_NOTES_TRANSFER
import com.maubis.scarlet.base.main.activity.KEEP_PACKAGE
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.support.BitmapHelper
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import java.io.File

class ShareToScarletRouterActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      val hasDirectIntent = handleDirectSendText(intent)
      if (hasDirectIntent) {
        startActivity(Intent(this, MainActivity::class.java))
        return
      }

      val note = handleSendText(intent)
      if (note === null) {
        return
      }
      startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
    } finally {
      finish()
    }
  }

  private fun handleSendText(intent: Intent): Note? {
    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
      ?: intent.getStringExtra(Intent.EXTRA_TITLE) ?: ""
    val sharedImages = when {
      intent.action == Intent.ACTION_SEND -> handleSendImage(intent)
      intent.action == Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleImages(intent)
      else -> emptyList()
    }
    if (sharedText.isBlank() && sharedSubject.isBlank() && sharedImages.isEmpty()) {
      return null
    }

    val note = when (isCallerKeep()) {
      true -> NoteBuilder().gen(sharedSubject, NoteBuilder().genFromKeep(sharedText))
      false -> NoteBuilder().gen(sharedSubject, sharedText)
    }
    note.save(this)

    val images = emptyList<File>().toMutableList()
    for (uri in sharedImages) {
      try {
        val inputStream = this.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val temporaryImage = createTempFile()
        BitmapHelper.saveToFile(temporaryImage, bitmap)

        images.add(sAppImageStorage.renameOrCopy(note, temporaryImage))
        temporaryImage.delete()
      } catch (exception: Exception) {
      }
    }
    val formats = note.getFormats().toMutableList()
    for (image in images.reversed()) {
      formats.add(0, Format(FormatType.IMAGE, image.name))
    }
    note.description = FormatBuilder().getSmarterDescription(formats)
    note.save(this)
    return note
  }

  private fun handleDirectSendText(intent: Intent): Boolean {
    val sharedText = intent.getStringExtra(INTENT_KEY_DIRECT_NOTES_TRANSFER)
    if (sharedText === null || sharedText.isBlank()) {
      return false
    }
    NoteImporter().gen(this, sharedText)
    return true
  }

  private fun handleSendImage(intent: Intent): List<Uri> {
    val images = emptyList<Uri>().toMutableList()
    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
      images.add(it)
    }
    return images
  }

  private fun handleSendMultipleImages(intent: Intent): List<Uri> {
    val images = emptyList<Uri>().toMutableList()
    intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
      for (parcelable in it) {
        if (parcelable is Uri) {
          images.add(parcelable)
        }
      }
    }
    return images
  }

  private fun isCallerKeep(): Boolean {
    return try {
      when {
        OsVersionUtils.canExtractReferrer() && (referrer?.toString()
          ?: "").contains(KEEP_PACKAGE) -> true
        callingPackage?.contains(KEEP_PACKAGE) ?: false -> true
        (intent?.`package` ?: "").contains(KEEP_PACKAGE) -> true
        else -> false
      }
    } catch (exception: Exception) {
      false
    }
  }
}
