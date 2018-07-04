package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.core.database.room.folder.Folder

fun Folder.getFirebaseFolder(): FirebaseFolder {
  return FirebaseFolder(
      uuid,
      title,
      timestamp,
      updateTimestamp,
      color
  )
}