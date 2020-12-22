package com.bijoysingh.quicknote.database

interface RemoteFolder<T, D> {

  fun initContentFolder(resourceId: T?, onSuccess: () -> Unit)

  fun initDeletedFolder(resourceId: T?, onSuccess: () -> Unit)

  fun insert(remoteData: RemoteUploadData, resource: D)

  fun delete(remoteData: RemoteUploadData)

  fun invalidate()
}