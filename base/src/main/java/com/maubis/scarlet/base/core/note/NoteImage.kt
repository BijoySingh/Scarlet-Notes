package com.maubis.scarlet.base.core.note

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File

interface ImageLoadCallback {
  fun onSuccess()

  fun onError()
}

class NoteImage(val context: Context) {
  fun renameOrCopy(note: Note, imageFile: File): File {
    val targetFile = getFile(note.uuid, RandomHelper.getRandom() + ".jpg")
    targetFile.mkdirs()
    deleteIfExist(targetFile)

    val renamed = imageFile.renameTo(targetFile)
    if (!renamed) {
      imageFile.copyTo(targetFile, true)
    }
    return targetFile
  }

  fun getFile(noteUUID: String, imageFormat: Format): File {
    if (imageFormat.formatType != FormatType.IMAGE) {
      throw IllegalArgumentException("Format should be an Image")
    }
    return getFile(noteUUID, imageFormat.text)
  }

  fun getFile(noteUUID: String, formatFileName: String): File {
    return File(context.cacheDir, "images" + File.separator + noteUUID + File.separator + formatFileName)
  }

  fun deleteAllFiles(note: Note) {
    for (format in FormatBuilder().getFormats(note.description)) {
      if (format.formatType === FormatType.IMAGE) {
        val file = getFile(note.uuid, format)
        deleteIfExist(file)
      }
    }
  }

  fun loadFileToImageView(image: ImageView, file: File, callback: ImageLoadCallback? = null) {
    Picasso.with(context).load(file).into(image, object : Callback {
      override fun onSuccess() {
        // Ignore successful call
        image.visibility = View.VISIBLE
        callback?.onSuccess()
      }

      override fun onError() {
        deleteIfExist(file)
        image.visibility = View.GONE
        callback?.onError()
      }
    })
  }

  companion object {
    fun deleteIfExist(file: File): Boolean {
      return when {
        file.exists() -> file.delete()
        else -> false
      }
    }
  }
}