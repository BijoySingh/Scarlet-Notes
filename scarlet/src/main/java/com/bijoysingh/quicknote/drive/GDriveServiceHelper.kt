package com.bijoysingh.quicknote.drive

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.support.v4.util.Pair
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.Executors

const val GOOGLE_DRIVE_ROOT_FOLDER = "Scarlet (App Data)"

const val GOOGLE_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
const val GOOGLE_DRIVE_FILE_MIME_TYPE = "text/plain"

const val INVALID_FILE_ID = "__invalid__"

class GDriveServiceHelper(private val mDriveService: Drive) {
  private val mExecutor = Executors.newSingleThreadExecutor()

  /**
   * Creates a text file in the user's My Drive folder and returns its file ID.
   */
  fun createFile(folderId: String, modificationTimeOverride: Long? = null): Task<String> {
    return Tasks.call(mExecutor, Callable {
      val metadata = File()
          .setParents(listOf(folderId))
          .setMimeType(GOOGLE_DRIVE_FILE_MIME_TYPE)
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
      // Retrieve the metadata as a File object.
      val metadata = mDriveService.files().get(fileId).execute()
      val name = metadata.name

      // Stream the file contents to a String.
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

  /**
   * Updates the file identified by `fileId` with the given `name` and `content`.
   */
  fun saveFile(fileId: String, name: String, content: String): Task<Void> {
    return Tasks.call(mExecutor, Callable<Void> {
      // Create a File containing any metadata changes.
      val metadata = File().setName(name)

      // Convert content to an AbstractInputStreamContent instance.
      val contentStream = ByteArrayContent.fromString("text/plain", content)

      // Update the metadata and contents.
      mDriveService.files().update(fileId, metadata, contentStream).execute()

      null
    })
  }

  fun getFilesInFolder(parentUid: String): Task<FileList> {
    return Tasks.call(mExecutor, Callable {
      mDriveService.files().list()
          .setSpaces("drive")
          .setPageSize(1000)
          .setQ("mimeType = '$GOOGLE_DRIVE_FILE_MIME_TYPE' and '$parentUid' in parents")
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

  /**
   * Returns an [Intent] for opening the Storage Access Framework file picker.
   */
  fun createFilePickerIntent(): Intent {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "text/plain"

    return intent
  }

  /**
   * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
   * created by [.createFilePickerIntent] using the given `contentResolver`.
   */
  fun openFileUsingStorageAccessFramework(
      contentResolver: ContentResolver, uri: Uri): Task<Pair<String, String>> {
    return Tasks.call(mExecutor, Callable {
      // Retrieve the document's display name from its metadata.
      var name: String = ""
      contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          name = cursor.getString(nameIndex)
        } else {
          throw IOException("Empty cursor returned for file.")
        }
      }

      // Read the document's contents as a String.
      var content: String = ""
      contentResolver.openInputStream(uri)!!.use { `is` ->
        BufferedReader(InputStreamReader(`is`)).use { reader ->
          val stringBuilder = StringBuilder()
          var line: String = reader.readLine()
          while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
          }
          content = stringBuilder.toString()
        }
      }

      Pair.create(name, content)
    })
  }
}
