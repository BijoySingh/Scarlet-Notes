package com.bijoysingh.quicknote.drive

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.Executors

const val GOOGLE_DRIVE_ROOT_FOLDER = "Scarlet (App Data)"

const val GOOGLE_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
const val GOOGLE_DRIVE_FILE_MIME_TYPE = "text/plain"
const val GOOGLE_DRIVE_IMAGE_MIME_TYPE = "image/jpeg"

const val INVALID_FILE_ID = "__invalid__"

class GDriveServiceHelper(private val mDriveService: Drive) {
  private val mExecutor = Executors.newSingleThreadExecutor()

  fun createFile(folderId: String, modificationTimeOverride: Long? = null, mimeType: String = GOOGLE_DRIVE_FILE_MIME_TYPE): Task<String> {
    return Tasks.call(mExecutor, Callable {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(mimeType)
          .setModifiedTime(DateTime(modificationTimeOverride ?: System.currentTimeMillis()))
          .setName("file")

      val googleFile = mDriveService.files().create(metadata).execute()
      googleFile?.id ?: INVALID_FILE_ID
    })
  }

  fun createFolder(parentUid: String, folderName: String): Task<String> {
    return Tasks.call(mExecutor, Callable {
      val metadata = File()
          .setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE)
          .setName(folderName)
      if (!parentUid.isEmpty()) {
        metadata.parents = listOf(parentUid)
      }
      val googleFile = mDriveService.files().create(metadata).execute()
      googleFile?.id ?: INVALID_FILE_ID
    })
  }


  fun readFile(fileId: String): Task<String> {
    return Tasks.call(mExecutor, Callable {
      val metadata = mDriveService.files().get(fileId).execute()
      val name = metadata.name

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

  fun saveFile(fileId: String, name: String, content: String): Task<Void> {
    return Tasks.call(mExecutor, Callable<Void> {
      val metadata = File().setName(name)
      val contentStream = ByteArrayContent.fromString("text/plain", content)
      mDriveService.files().update(fileId, metadata, contentStream).execute()
      null
    })
  }

  fun saveFile(fileId: String, file: java.io.File): Task<Void> {
    return Tasks.call(mExecutor, Callable<Void> {
      val metadata = File().setName(file.name)
      val mediaContent = FileContent(GOOGLE_DRIVE_IMAGE_MIME_TYPE, file)
      mDriveService.files().update(fileId, metadata, mediaContent).execute()
      null
    })
  }

  fun getFilesInFolder(parentUid: String, mimeType: String = GOOGLE_DRIVE_FILE_MIME_TYPE): Task<FileList> {
    return Tasks.call(mExecutor, Callable {
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
    return Tasks.call(mExecutor, Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setQ(query)
          .execute()
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
}
