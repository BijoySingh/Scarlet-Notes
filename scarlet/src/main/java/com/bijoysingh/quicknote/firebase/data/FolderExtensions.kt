package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.database.room.folder.Folder

// TODO: Remove this on Firebase deprecation
fun Folder.getFirebaseFolder(): FirebaseFolder {
  return FirebaseFolder(
    uuid,
    title,
    timestamp,
    updateTimestamp,
    color
  )
}