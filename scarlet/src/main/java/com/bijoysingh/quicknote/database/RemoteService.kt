package com.bijoysingh.quicknote.database

import java.io.File

open class RemoteResourceId

interface IRemoteService<T : RemoteResourceId> {
  fun readFile(remoteDataType: RemoteUploadData, onRead: (String) -> Unit)

  fun readIntoFile(remoteDataType: RemoteUploadData, file: File, onRead: (Boolean) -> Unit)

  fun createDirectory(parentResourceId: T?, directoryName: String, onSuccess: (T?) -> Unit)

  fun getOrCreateDirectory(parentResourceId: T?, directoryName: String, onSuccess: (T?) -> Unit)

  fun getDirectories(parentResourceId: T, directoryNames: List<String>, onSuccess: (List<Pair<String, T?>>) -> Unit)
}
