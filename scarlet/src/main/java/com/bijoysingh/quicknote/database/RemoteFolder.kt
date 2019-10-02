package com.bijoysingh.quicknote.database

interface RemoteFolder<T: RemoteResourceId, D> {

  fun initContentFolder(resourceId: T?, onSuccess: () -> Unit)

  fun initDeletedFolder(resourceId: T?, onSuccess: () -> Unit)

  fun insert(remoteDataType: RemoteUploadData, resource: D)

  fun delete(remoteDataType: RemoteUploadData)

  fun invalidate()
}