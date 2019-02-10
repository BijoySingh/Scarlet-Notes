package com.maubis.scarlet.base.support.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong

const val IMAGE_CACHE_SIZE = 1024 * 1024 * 10L

class ImageCache(context: Context) {

  private val persistentFolder = File(context.filesDir, "images")
  private val thumbnailFolder = File(context.cacheDir, "thumbnails")
  private var thumbnailCacheSize = AtomicLong(0L)

  init {
    thumbnailFolder.mkdirs()
    persistentFolder.mkdirs()

    GlobalScope.launch {
      val files = thumbnailFolder.listFiles()
      files?.forEach { thumbnailCacheSize.addAndGet(it.length()) }
      performEviction()
    }
  }

  fun persistentFile(noteUUID: String, formatFileName: String): File {
    val folder = File(persistentFolder, noteUUID)
    folder.mkdirs()

    return File(folder, formatFileName)
  }

  fun thumbnailFile(noteUUID: String, formatFileName: String): File {
    val folder = File(thumbnailFolder, noteUUID)
    folder.mkdirs()

    return File(folder, formatFileName)
  }

  fun loadFromCache(cacheFile: File): Bitmap? {
    if (cacheFile.exists()) {
      val options = BitmapFactory.Options()
      options.inPreferredConfig = Bitmap.Config.ARGB_8888
      return BitmapFactory.decodeFile(cacheFile.absolutePath, options)
    }
    return null
  }

  fun saveThumbnail(cacheFile: File, bitmap: Bitmap): Bitmap {
    if (cacheFile.exists()) {
      thumbnailCacheSize.addAndGet(-cacheFile.length())
    }

    val fOut = FileOutputStream(cacheFile)
    val compressedBitmap = sampleBitmap(bitmap)
    compressedBitmap.compress(Bitmap.CompressFormat.PNG, 75, fOut)
    fOut.flush()
    fOut.close()

    performEviction()
    return compressedBitmap
  }

  private fun sampleBitmap(bitmap: Bitmap): Bitmap {
    val cropDimension = Math.min(bitmap.width, bitmap.height)
    var destinationBitmap = Bitmap.createBitmap(bitmap, 0, 0, cropDimension, cropDimension)
    return Bitmap.createScaledBitmap(destinationBitmap, 256, 256, false)
  }

  @Synchronized
  private fun performEviction() {
    if (thumbnailCacheSize.get() <= IMAGE_CACHE_SIZE) {
      return
    }

    GlobalScope.async {
      var index = 0
      val files = thumbnailFolder.listFiles().sortedBy { it.lastModified() }
      while (thumbnailCacheSize.get() > IMAGE_CACHE_SIZE * 0.9 && index < files.size) {
        thumbnailCacheSize.addAndGet(-files[index].length())
        files[index].delete()
        index++
      }
    }
  }
}