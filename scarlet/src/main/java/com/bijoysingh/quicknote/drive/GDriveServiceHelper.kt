package com.bijoysingh.quicknote.drive

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.maubis.scarlet.base.config.CoreConfig
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

const val GOOGLE_DRIVE_ROOT_FOLDER = "Scarlet (App Data)"

const val GOOGLE_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
const val GOOGLE_DRIVE_FILE_MIME_TYPE = "text/plain"
const val GOOGLE_DRIVE_IMAGE_MIME_TYPE = "image/jpeg"

const val INVALID_FILE_ID = "__invalid__"

const val STORE_KEY_G_DRIVE_LAST_MODIFICATION_TIME = "store_key_g_drive_last_modification_time"
var sGDriveLastModificationTime: Long
  get() = CoreConfig.instance.store().get(STORE_KEY_G_DRIVE_LAST_MODIFICATION_TIME, 0L)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_G_DRIVE_LAST_MODIFICATION_TIME, value)

var updateCheckerFileId: String? = null

class ErrorCallable<T>(val callable: Callable<T>) : Callable<T> {
  override fun call(): T {
    try {
      return callable.call()
    } catch (exception: Exception) {
      Log.e("GoogleDrive", exception.message, exception)
      throw exception
    }
  }
}

fun getTrueCurrentTime(): Long {
  var calendar: Calendar = Calendar.getInstance()
  try {
    calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
  } catch (exception: Exception) {
  }
  return calendar.timeInMillis
}

class GDriveServiceHelper(private val mDriveService: Drive) {
  private val mExecutor = Executors.newFixedThreadPool(8)
  private val mSerialExecutor = Executors.newSingleThreadExecutor()

  fun <T> execute(callable: Callable<T>): Task<T> {
    return Tasks.call(mExecutor, ErrorCallable(callable))
  }

  fun createFileWithData(folderId: String, name: String, content: String = ""): Task<String> {
    val currentTime = getTrueCurrentTime()
    val contentToSave = if (content.isEmpty()) currentTime.toString() else content
    return execute(Callable {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(GOOGLE_DRIVE_FILE_MIME_TYPE)
          .setModifiedTime(DateTime(currentTime))
          .setName(name)
      val contentStream = ByteArrayContent.fromString("text/plain", contentToSave)
      val googleFile = mDriveService.files().create(metadata, contentStream).execute()
      googleFile?.id ?: INVALID_FILE_ID
    })
  }

  fun createFileWithData(folderId: String, name: String, file: java.io.File): Task<String> {
    val currentTime = getTrueCurrentTime()
    return execute(Callable<String> {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(GOOGLE_DRIVE_IMAGE_MIME_TYPE)
          .setModifiedTime(DateTime(currentTime))
          .setName(name)
      val mediaContent = FileContent(GOOGLE_DRIVE_IMAGE_MIME_TYPE, file)
      val googleFile = mDriveService.files().create(metadata, mediaContent).execute()
      googleFile?.id ?: INVALID_FILE_ID
    })
  }

  fun createFolder(parentUid: String, folderName: String): Task<String> {
    return execute(Callable {
      val metadata = File()
          .setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE)
          .setModifiedTime(DateTime(getTrueCurrentTime()))
          .setName(folderName)
      if (!parentUid.isEmpty()) {
        metadata.parents = listOf(parentUid)
      }
      val googleFile = mDriveService.files().create(metadata).execute()
      googleFile?.id ?: INVALID_FILE_ID
    })
  }


  fun readFile(fileId: String): Task<String> {
    return execute(Callable {
      mDriveService.files().get(fileId).executeMediaAsInputStream().use { `is` ->
        BufferedReader(InputStreamReader(`is`)).use { reader ->
          val stringBuilder = StringBuilder()
          var line: String? = reader.readLine()
          while (line !== null) {
            stringBuilder.append(line)
            line = reader.readLine()
          }
          val contents = stringBuilder.toString()
          contents
        }
      }
    })
  }

  fun readFile(fileId: String, destinationFile: java.io.File): Task<Void> {
    return execute(Callable<Void> {
      destinationFile.parentFile.mkdirs()
      val fileStream = FileOutputStream(destinationFile)
      mDriveService.files().get(fileId).executeMediaAndDownloadTo(fileStream)
      null
    })
  }

  fun saveFile(fileId: String, name: String, content: String): Task<Void> {
    return execute(Callable<Void> {
      val metadata = File().setName(name)
      val contentStream = ByteArrayContent.fromString("text/plain", content)
      mDriveService.files().update(fileId, metadata, contentStream).execute()
      null
    })
  }

  fun getFilesInFolder(parentUid: String, mimeType: String = GOOGLE_DRIVE_FILE_MIME_TYPE): Task<FileList> {
    return execute(Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setPageSize(1000)
          .setQ("mimeType = '$mimeType' and '$parentUid' in parents")
          .execute()
    })
  }

  fun getFolderQuery(parentUid: String, name: String): Task<FileList> {
    val query = when {
      parentUid.isEmpty() -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name'"
      else -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name' and '$parentUid' in parents"
    }
    return execute(Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ(query)
          .execute()
    })
  }

  fun getSubRootFolders(parentUid: String, names: List<String>): Task<FileList> {
    var nameQueryBuilder = "name = '${names[0]}'"
    names.subList(1, names.lastIndex).forEach {
      nameQueryBuilder += " or name = '$it'"
    }
    return execute(Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ("mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and ($nameQueryBuilder) and '$parentUid' in parents")
          .execute()
    })
  }

  fun removeFileOrFolder(fileUid: String): Task<Void> {
    return execute(Callable<Void> {
      mDriveService.files().delete(fileUid)
      null
    })
  }

  fun getOrCreateDirectory(parentUid: String, name: String, onFolderId: (String?) -> Unit) {
    getFolderQuery(parentUid, name).addOnCompleteListener { getTask ->
      val fid = getTask.result?.files?.firstOrNull()?.id
      if (fid !== null) {
        onFolderId(fid)
        return@addOnCompleteListener
      }

      createFolder(parentUid, name).addOnCompleteListener { createTask ->
        onFolderId(createTask.result)
      }
    }
  }

  fun getLastUpdateTime(): Task<File> {
    val folderId = updateCheckerFileId
    return Tasks.call(mSerialExecutor, ErrorCallable(Callable<File> {
      when {
        (folderId === null) -> throw RuntimeException("Folder not set")
        else -> mDriveService.files().get(folderId).execute()
      }
    }))
  }

  fun updateLastModifiedTime(folderUid: String): Long {
    val currentTime = getTrueCurrentTime()
    Tasks.call(mSerialExecutor, ErrorCallable(Callable<Unit> {
      val metadata = File()
          .setModifiedTime(DateTime(currentTime))
          .setModifiedByMeTime(DateTime(currentTime))
      mDriveService.files().update(folderUid, metadata).execute()
    }))
    return currentTime
  }
}
