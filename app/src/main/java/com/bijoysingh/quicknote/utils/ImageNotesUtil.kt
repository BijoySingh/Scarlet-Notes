package com.bijoysingh.quicknote.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.getFormats
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.github.bijoysingh.starter.util.RandomHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File

fun renameOrCopy(context: Context, note: Note, imageFile: File): File {
  val targetFile = getFile(context, note.uuid, RandomHelper.getRandom() + ".jpg")
  targetFile.mkdirs()
  targetFile.deleteIfExist()
  val renamed = imageFile.renameTo(targetFile)
  if (!renamed) {
    imageFile.copyTo(targetFile, true)
  }
  return targetFile
}

fun getFile(context: Context, noteUUID: String, format: Format): File {
  return getFile(context, noteUUID, format.text)
}

fun getFile(context: Context, noteUUID: String, formatFileName: String): File {
  return File(context.cacheDir, "images" + File.separator + noteUUID + File.separator + formatFileName)
}

fun deleteAllFiles(context: Context, note: Note) {
  for (format in note.getFormats()) {
    if (format.formatType === FormatType.IMAGE) {
      val file = getFile(context, note.uuid, format)
      file.deleteIfExist()
    }
  }
}

fun loadFileToImageView(context: Context, image: ImageView, file: File, callback: Callback? = null) {
  Picasso.with(context).load(file).into(image, object : Callback {
    override fun onSuccess() {
      // Ignore successful call
      image.visibility = View.VISIBLE
      callback?.onSuccess()
    }

    override fun onError() {
      file.deleteIfExist()
      image.visibility = View.GONE
      callback?.onError()
    }
  })
}

fun deleteAllRedundantImageFiles(context: Context) {
  val imagesFolder = File(context.cacheDir, "images" + File.separator)
  val availableDirectories = HashSet<String>()
  for (file in imagesFolder.listFiles()) {
    if (file.isDirectory) {
      availableDirectories.add(file.name)
    }
  }
  val ids = Note.db().allUUIDs
  for (id in ids) {
    availableDirectories.remove(id)
  }
  for (uuid in availableDirectories) {
    val noteFolder = File(imagesFolder, uuid)
    for (file in noteFolder.listFiles()) {
      file.deleteIfExist()
    }
  }
}