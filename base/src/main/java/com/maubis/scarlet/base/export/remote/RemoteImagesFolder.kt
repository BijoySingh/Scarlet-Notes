package com.maubis.scarlet.base.export.remote

import android.content.Context
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.support.utils.ImageCache
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class RemoteImagesFolder(context: Context, val folder: File) {

  val internalCache = ImageCache(context)

  // note uuid => paths map
  val externalCache = ConcurrentHashMap<String, Set<String>>()

  init {
    GlobalScope.launch {
      folder.mkdirs()
      scan()
    }
  }

  fun scan() {
    val noteUuids = folder.listFiles() ?: emptyArray()
    noteUuids.forEach { note ->
      if (note.isDirectory) {
        val imageUuids = note.listFiles() ?: emptyArray()
        externalCache[note.name] = imageUuids.filter { it.isFile }.map { it.absolutePath }.toSet()
      }
    }
  }

  fun onInsert(note: ExportableNote) {
    GlobalScope.launch {
      val files = FormatBuilder().getFormats(note.description()).filter { it.formatType == FormatType.IMAGE }.map { it.text }.toSet()
      val externalFiles = externalCache[note.uuid()] ?: emptySet()
      val externalFileNames = externalFiles.map { File(it).name }
      externalFiles.forEach {
        val imageFile = File(it)
        if (!files.contains(imageFile.name)) {
          imageFile.delete()
        }
      }
      files.forEach {
        if (!externalFileNames.contains(it)) {
          // Copy the images over
          val sourceFile = internalCache.persistentFile(note.uuid(), it)
          val destinationFolder = File(folder, note.uuid())
          destinationFolder.mkdirs()
          val destinationFile = File(destinationFolder, it)
          copy(sourceFile, destinationFile)
        }
      }
      externalCache[note.uuid()] = files.toSet()
    }
  }

  fun onRemove(uuid: String) {
    GlobalScope.launch {
      val noteFolder = File(folder, uuid)
      noteFolder.deleteRecursively()
      externalCache.remove(uuid)
    }
  }

  fun onRemoteInsert(note: ExportableNote) {
    GlobalScope.launch {
      val noteFolder = File(folder, note.uuid())
      val imageFiles = noteFolder.listFiles() ?: emptyArray()
      imageFiles.filter { it.isFile }

      val imagesKnown = internalCache.imagesForNote(note.uuid()).map { it.name }
      imageFiles.forEach {
        if (!imagesKnown.contains(it.name)) {
          // Put it into the internal cache
          val bitmap = internalCache.loadFromCache(it)
          if (bitmap !== null) {
            internalCache.saveToCache(internalCache.persistentFile(note.uuid, it.name), bitmap)
          }
        }
      }
    }
  }

  fun deleteEverything() {
    GlobalScope.launch {
      externalCache.clear()
      folder.deleteRecursively()
      folder.mkdirs()
    }
  }

  private fun copy(src: File, dst: File) {
    try {
      val inStream = FileInputStream(src)
      val outStream = FileOutputStream(dst)
      val inChannel = inStream.getChannel()
      val outChannel = outStream.getChannel()
      inChannel.transferTo(0, inChannel.size(), outChannel)
      inStream.close()
      outStream.close()
    } catch (exception: Exception) {
      maybeThrow(exception)
    }
  }
}