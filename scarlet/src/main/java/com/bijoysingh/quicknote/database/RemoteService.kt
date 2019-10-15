package com.bijoysingh.quicknote.database

import java.io.File

interface IRemoteService<ResourceIdType, FileType, FileListType> {

  fun createDirectory(parentResourceId: ResourceIdType?, directoryName: String, onSuccess: (ResourceIdType?) -> Unit)

  fun getOrCreateDirectory(parentResourceId: ResourceIdType?, directoryName: String, onSuccess: (ResourceIdType?) -> Unit)

  fun getDirectories(parentResourceId: ResourceIdType, directoryNames: List<String>, onSuccess: (List<Pair<String, ResourceIdType>>) -> Unit)

  fun createFileWithData(parentResourceId: ResourceIdType, name: String, content: String, updateTime: Long, onSuccess: (FileType?) -> Unit)
  fun createFileFromFile(parentResourceId: ResourceIdType, name: String, localFile: File, updateTime: Long, onSuccess: (FileType?) -> Unit)

  fun updateFileWithData(resourceId: ResourceIdType, name: String, content: String, updateTime: Long, onSuccess: (FileType?) -> Unit)

  fun readFile(resourceId: ResourceIdType, onRead: (String) -> Unit)
  fun readIntoFile(resourceId: ResourceIdType, destinationFile: File, onRead: (Boolean) -> Unit)

  fun removeFileOrFolder(resourceId: ResourceIdType, onSuccess: (Boolean) -> Unit)

  fun getFilesInFolder(parentResourceId: ResourceIdType, mimeType: String, onSuccess: (FileListType?) -> Unit)
}
