package com.bijoysingh.quicknote.drive

import android.os.SystemClock
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.database.IRemoteService
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
var sSyncingCount: AtomicLong = AtomicLong(0)

class CountingErrorCallable<T>(val action: String, callable: Callable<T>) : Callable<T?> {
  val errorCallable = ErrorCallable(action, callable)

  override fun call(): T? {
    try {
      sSyncingCount.incrementAndGet()
      gDrive?.notifyPendingSyncChange(action)
      return errorCallable.call()
    } catch (exception: Exception) {
      return throwOrReturn(exception, null)
    } finally {
      sSyncingCount.decrementAndGet()
      gDrive?.notifyPendingSyncChange(action)
    }
  }
}

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
    } catch (exception: ClassNotFoundException) {
      return throwOrReturn(exception, null)
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

class GDriveServiceHelper(private val mDriveService: Drive) : IRemoteService<String, File, FileList> {
  private val mExecutor = Executors.newFixedThreadPool(4)
  private fun <T> execute(action: String = "", callable: Callable<T>): Task<T?> {
    return Tasks.call(mExecutor, CountingErrorCallable(action, callable))
  }

  override fun createDirectory(parentResourceId: String?, directoryName: String, onSuccess: (String?) -> Unit) {
    val parentUid = parentResourceId ?: ""
    log("GDrive", "createDirectory($parentUid, $directoryName)")
    execute("createDirectory", Callable {
      try {
        val timestamp = DateTime(getTrueCurrentTime())
        val metadata = File()
            .setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE)
            .setModifiedTime(timestamp)
            .setName(directoryName)
        if (!parentUid.isEmpty()) {
          metadata.parents = listOf(parentUid)
        }
        mDriveService.files().create(metadata).execute()
      } catch (exception: Exception) {
        throwOrReturn(exception, null)
      }
    }).addOnCompleteListener { result ->
      onSuccess(result.result?.id ?: INVALID_FILE_ID)
    }
  }

  override fun getOrCreateDirectory(parentResourceId: String?, directoryName: String, onSuccess: (String?) -> Unit) {
    val parentUid = parentResourceId ?: ""
    log("GDrive", "getOrCreateDirectory($parentUid, $directoryName)")

    if (parentResourceId === null) {
      return createDirectory(null, directoryName, onSuccess)
    }

    getDirectory(parentResourceId, directoryName).addOnCompleteListener { getTask ->
      val fid = getTask.result?.files?.firstOrNull()?.id
      if (fid !== null) {
        onSuccess(fid)
        return@addOnCompleteListener
      }

      createDirectory(parentResourceId, directoryName) { uuid ->
        onSuccess(uuid ?: INVALID_FILE_ID)
      }
    }
  }

  override fun getDirectories(parentResourceId: String, directoryNames: List<String>, onSuccess: (List<Pair<String, String>>) -> Unit) {
    log("GDrive", "getSubRootFolders($parentResourceId, $directoryNames)")
    var nameQueryBuilder = "name = '${directoryNames[0]}'"
    directoryNames.subList(1, directoryNames.lastIndex + 1).forEach {
      nameQueryBuilder += " or name = '$it'"
    }
    execute("getSubRootFolders", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ("mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and ($nameQueryBuilder) and '$parentResourceId' in parents")
          .setOrderBy("modifiedTime desc")
          .execute()
    }).addOnCompleteListener { result ->
      val files = result.result?.files ?: emptyList()
      val namesIdList = emptyList<Pair<String, String>>().toMutableList()
      files.forEach {
        namesIdList.add(Pair(it.name, it.id))
      }
      onSuccess(namesIdList)
    }
  }

  override fun createFileWithData(parentResourceId: String, name: String, content: String, updateTime: Long, onSuccess: (File?) -> Unit) {
    log("GDrive", "createFileWithData($parentResourceId, $name)")
    val contentToSave = if (content.isEmpty()) updateTime.toString() else content
    execute("createFileWithData", Callable {
      try {
        val metadata = File()
            .setParents(listOf(parentResourceId))
            .setMimeType(GOOGLE_DRIVE_FILE_MIME_TYPE)
            .setModifiedTime(DateTime(updateTime))
            .setName(name)
        val contentStream = ByteArrayContent.fromString("text/plain", contentToSave)
        mDriveService.files().create(metadata, contentStream).execute()
      } catch (exception: Exception) {
        throwOrReturn(exception, null)
      }
    }).addOnCompleteListener { result -> onSuccess(result.result) }
  }

  override fun updateFileWithData(resourceId: String, name: String, content: String, updateTime: Long, onSuccess: (File?) -> Unit) {
    log("GDrive", "saveFile($resourceId, $name)")
    execute("saveFile", Callable {
      try {
        val metadata = File()
            .setModifiedTime(DateTime(updateTime))
            .setName(name)
        val contentStream = ByteArrayContent.fromString("text/plain", content)
        mDriveService.files().update(resourceId, metadata, contentStream).execute()
      } catch (exception: Exception) {
        throwOrReturn(exception, null)
      }
    }).addOnCompleteListener { result -> onSuccess(result.result) }
  }

  override fun createFileFromFile(parentResourceId: String, name: String, localFile: java.io.File, updateTime: Long, onSuccess: (File?) -> Unit) {
    log("GDrive", "createFileWithData($parentResourceId, $name, ${localFile.absolutePath})")
    execute("createFileWithData", Callable {
      try {
        val metadata = File()
            .setParents(listOf(parentResourceId))
            .setMimeType(GOOGLE_DRIVE_IMAGE_MIME_TYPE)
            .setModifiedTime(DateTime(updateTime))
            .setName(name)
        val mediaContent = FileContent(GOOGLE_DRIVE_IMAGE_MIME_TYPE, localFile)
        mDriveService.files().create(metadata, mediaContent).execute()
      } catch (exception: Exception) {
        throwOrReturn(exception, null)
      }
    }).addOnCompleteListener { result -> onSuccess(result.result) }
  }

  override fun readFile(resourceId: String, onRead: (String) -> Unit) {
    log("GDrive", "readFile($resourceId)")
    execute("readFile", Callable {
      try {
        mDriveService.files().get(resourceId).executeMediaAsInputStream().use { `is` ->
          BufferedReader(InputStreamReader(`is`)).use { reader ->
            reader.readText()
          }
        }
      } catch (exception: Exception) {
        throwOrReturn(exception, null)
      }
    }).addOnCompleteListener { result ->
      onRead(result.result ?: "")
    }
  }

  override fun readIntoFile(resourceId: String, destinationFile: java.io.File, onRead: (Boolean) -> Unit) {
    log("GDrive", "readFile($resourceId, ${destinationFile.absolutePath})")
    execute("readFile", Callable<Boolean> {
      destinationFile.parentFile.mkdirs()
      try {
        val fileStream = FileOutputStream(destinationFile)
        mDriveService.files().get(resourceId).executeMediaAndDownloadTo(fileStream)
        fileStream.close()
      } catch (exception: Exception) {
        return@Callable throwOrReturn(exception, false)
      }
      destinationFile.exists()
    }).addOnCompleteListener { result ->
      onRead(result.result ?: false)
    }
  }

  override fun removeFileOrFolder(resourceId: String, onSuccess: (Boolean) -> Unit) {
    log("GDrive", "removeFileOrFolder($resourceId)")
    execute("removeFileOrFolder", Callable<Boolean> {
      try {
        mDriveService.files().delete(resourceId).execute()
        true
      } catch (exception: Exception) {
        maybeThrow(exception)
        false
      }
    }).addOnCompleteListener { result -> onSuccess(result.result ?: false) }
  }

  override fun getFilesInFolder(parentResourceId: String, mimeType: String, onSuccess: (FileList?) -> Unit) {
    log("GDrive", "getFilesInFolder($parentResourceId, $mimeType)")
    execute("getFilesInFolder", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setPageSize(1000)
          .setFields("files(name, id, modifiedTime, mimeType)")
          .setQ("mimeType = '$mimeType' and '$parentResourceId' in parents")
          .setOrderBy("modifiedTime desc")
          .execute()
    }).addOnCompleteListener { result ->
      onSuccess(result.result)
    }
  }

  private fun getDirectory(parentUid: String, name: String): Task<FileList?> {
    log("GDrive", "getFolderQuery($parentUid, $name)")
    val query = when {
      parentUid.isEmpty() -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name'"
      else -> "mimeType = '$GOOGLE_DRIVE_FOLDER_MIME_TYPE' and name = '$name' and '$parentUid' in parents"
    }
    return execute("getFolderQuery", Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ(query)
          .setOrderBy("modifiedTime desc")
          .execute()
    })
  }

}
