package com.maubis.scarlet.base.support

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object BitmapHelper {
  fun send(context: Context, bitmap: Bitmap) {
    val path = MediaStore.Images.Media.insertImage(
      context.contentResolver, bitmap, "Scarlet Image", "Scarlet Image")
    val uri = Uri.parse(path)

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/jpeg"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(intent, "Share Image"))
  }

  fun send(context: Context, bitmaps: List<Bitmap>) {
    val fileUris = ArrayList<Uri>()
    bitmaps
      .mapIndexed { index, bitmap ->
        MediaStore.Images.Media.insertImage(
          context.contentResolver, bitmap, "Scarlet Image ($index)", "Scarlet Image ($index)")
      }.map {
        Uri.parse(it)
      }.forEach { fileUris.add(it) }
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    intent.type = "image/jpeg"
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
    context.startActivity(Intent.createChooser(intent, "Share Images"))
  }

  fun loadFromFile(cacheFile: File): Bitmap? {
    if (cacheFile.exists()) {
      val options = BitmapFactory.Options()
      options.inPreferredConfig = Bitmap.Config.ARGB_8888
      return BitmapFactory.decodeFile(cacheFile.absolutePath, options)
    }
    return null
  }

  fun saveToFile(cacheFile: File, bitmap: Bitmap) {
    val fOut = FileOutputStream(cacheFile)
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
    fOut.flush()
    fOut.close()
  }
}