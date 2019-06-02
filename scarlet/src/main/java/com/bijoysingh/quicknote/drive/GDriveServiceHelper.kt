package com.bijoysingh.quicknote.drive

import android.os.SystemClock
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow
import com.maubis.scarlet.base.support.utils.throwOrReturn
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

const val GOOGLE_DRIVE_ROOT_FOLDER = "Scarlet (App Data)"

const val GOOGLE_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
const val GOOGLE_DRIVE_FILE_MIME_TYPE = "text/plain"
const val GOOGLE_DRIVE_IMAGE_MIME_TYPE = "image/jpeg"

const val INVALID_FILE_ID = "__invalid__"
const val MAX_THRESHOLD_QUERIES_PER_SECOND = 4
const val MIN_RESET_QUERIES_PER_SECOND = 0.1

var lastCheckpointTime: AtomicLong = AtomicLong(0L)
var numQueriesSinceLastCheckpoint: AtomicLong = AtomicLong(0L)

class ErrorCallable<T>(val action: String, val callable: Callable<T>) : Callable<T?> {
  private var delay: Long = 200L

  override fun call(): T? {
    val lastCheckpoint = lastCheckpointTime.get()
    if (lastCheckpoint == 0L) {
      lastCheckpointTime.set(System.currentTimeMillis())
    }

    val currentCount = numQueriesSinceLastCheckpoint.get() * 1.0
    val deltaTimeS = (System.currentTimeMillis() - lastCheckpointTime.get()) / 1000.0
    val currentQueriesPerSecond = (currentCount / deltaTimeS)
    // log("GDrive", "Request being called: action=$action, currentCount=$currentCount, deltaTimeS=$deltaTimeS, requestRate=$currentQueriesPerSecond")
    if (currentCount >= 10 && deltaTimeS > 0) {
      when {
        currentQueriesPerSecond > MAX_THRESHOLD_QUERIES_PER_SECOND -> {
          // log("GDrive", "Rate limiting measures taken: action=$action, currentCount=$currentCount, deltaTimeS=$deltaTimeS, delay=$delay")
          SystemClock.sleep(delay)
          return call()
        }
        (currentCount / deltaTimeS) < MIN_RESET_QUERIES_PER_SECOND -> {
          numQueriesSinceLastCheckpoint.set(0L)
          lastCheckpointTime.set(SystemClock.currentThreadTimeMillis())
        }
      }
    }

    try {
      numQueriesSinceLastCheckpoint.addAndGet(1)
      return callable.call()
    } catch (exception: InterruptedIOException) {
      // Ignore timeout exceptions
      return null
    } catch (exception: Exception) {
      return throwOrReturn(exception, null)
    }
  }
}

fun getTrueCurrentTime(): Long {
  var calendar: Calendar = Calendar.getInstance()
  try {
    calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
  } catch (exception: Exception) {
    maybeThrow(exception)
  }
  return calendar.timeInMillis
}

class GDriveServiceHelper(private val mDriveService: Drive) {
  private val mExecutor = Executors.newFixedThreadPool(4)

  fun <T> execute(action: String = "", callable: Callable<T>): Task<T?> {
    return Tasks.call(mExecutor, ErrorCallable(action, callable))
  }

  fun createFileWithData(folderId: String, name: String, content: String, updateTime: Long): Task<File?> {
    log("GDrive", "createFileWithData($folderId, $name)")
    val contentToSave = if (content.isEmpty()) updateTime.toString() else content
    return execute("createFileWithData", Callable {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(GOOGLE_DRIVE_FILE_MIME_TYPE)
          .setModifiedTime(DateTime(updateTime))
          .setName(name)
      val contentStream = ByteArrayContent.fromString("text/plain", contentToSave)
      mDriveService.files().create(metadata, contentStream).execute()
    })
  }

  fun createFileWithData(folderId: String, name: String, file: java.io.File, updateTime: Long): Task<File?> {
    log("GDrive", "createFileWithData($folderId, $name, ${file.absolutePath})")
    return execute("createFileWithData", Callable<File> {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(GOOGLE_DRIVE_IMAGE_MIME_TYPE)
          .setModifiedTime(DateTime(updateTime))
          .setName(name)
      val mediaContent = FileContent(GOOGLE_DRIVE_IMAGE_MIME_TYPE, file)
      mDriveService.files().create(metadata, mediaContent).execute()
    })
  }

  fun createFolder(parentUid: String, folderName: String): Task<File?> {
    log("GDrive", "createFolder($parentUid, $folderName)")
    return execute("createFolder", Callable {
      val metadata = File()
          .setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE)
          .setModifiedTime(DateTime(getTrueCurrentTime()))
          .setName(folderName)
      if (!parentUid.isEmpty()) {
        metadata.parents = listOf(parentUid)
      }
      mDriveService.files().create(metadata).execute()
    })
  }

  fun readFile(fileId: String): Task<String?> {
    log("GDrive", "readFile($fileId)")
    return execute("readFile", Callable {
      mDriveService.files().get(fileId).executeMediaAsInputStream().use { `is` ->
        BufferedReader(InputStreamReader(`is`)).use { reader ->
          reader.readText()
        }
      }
    })
  }

  fun readFile(fileId: String, destinationFile: java.io.File): Task<Boolean?> {
    log("GDrive", "readFile($fileId, ${destinationFile.absolutePath})")
    return execute("readFile", Callable<Boolean> {
      destinationFile.parentFile.mkdirs()
      try {
        val fileStream = FileOutputStream(destinationFile)
        mDriveService.files().get(fileId).executeMediaAndDownloadTo(fileStream)
        fileStream.close()
      } catch (exception: Exception) {
        return@Callable throwOrReturn(exception, false)
      }
      destinationFile.exists()
    })
  }

  fun saveFile(fileId: String, name: String, content: String, updateTime: Long): Task<File?> {
    log("GDrive", "saveFile($fileId, $name)")
    return execute("saveFile", Callable<File> {
      val metadata = File().setModifiedTime(DateTime(updateTime)).setName(name)
      val contentStream = ByteArrayContent.fromString("text/plain", content)
      mDriveService.files().update(fileId, metadata, contentStream).execute()
    })
  }

  fun getFilesInFolder(parentUid: String, mimeType: String = GOOGLE_DRIVE_FILE_MIME_TYPE): Task<FileList?> {
    log("GDrive", "getFilesInFolder($parentUid, $mimeType)")
    return execute("getFilesInFolder", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setPageSize(1000)
          .setFields("files(name, id, modifiedTime, mimeType)")
          .setQ("mimeType = '$mimeType' and '$parentUid' in parents")
          .setOrderBy("modifiedTime desc")
          .execute()
    })
  }

  fun getFolderQuery(parentUid: String, name: String): Task<FileList?> {
    log("GDrive", "getFolderQuery($parentUid, $name)")
    val query = when {
      parentUid.isEmpty() -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name'"
      else -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name' and '$parentUid' in parents"
    }
    return execute("getFolderQuery", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ(query)
          .execute()
    })
  }

  fun getSubRootFolders(parentUid: String, names: List<String>): Task<FileList?> {
    log("GDrive", "getSubRootFolders($parentUid, $names)")
    var nameQueryBuilder = "name = '${names[0]}'"
    names.subList(1, names.lastIndex + 1).forEach {
      nameQueryBuilder += " or name = '$it'"
    }
    return execute("getSubRootFolders", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ("mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and ($nameQueryBuilder) and '$parentUid' in parents")
          .execute()
    })
  }

  fun removeFileOrFolder(fileUid: String): Task<Void?> {
    log("GDrive", "removeFileOrFolder($fileUid)")
    return execute("removeFileOrFolder", Callable<Void> {
      mDriveService.files().delete(fileUid).execute()
      null
    })
  }

  fun getOrCreateDirectory(parentUid: String, name: String, onFolderId: (String?) -> Unit) {
    log("GDrive", "getOrCreateDirectory($parentUid, $name)")
    getFolderQuery(parentUid, name).addOnCompleteListener { getTask ->
      val fid = getTask.result?.files?.firstOrNull()?.id
      if (fid !== null) {
        onFolderId(fid)
        return@addOnCompleteListener
      }

      createFolder(parentUid, name).addOnCompleteListener { createTask ->
        onFolderId(createTask.result?.id ?: INVALID_FILE_ID)
      }
    }
  }
}
