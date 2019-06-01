package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.database.room.note.Note

// TODO: Remove this on Firebase deprecation
fun Note.getFirebaseNote(): FirebaseNote {
  return FirebaseNote(
      uuid,
      description,
      timestamp,
      updateTimestamp,
      color,
      state,
      if (tags == null) "" else tags,
      locked,
      pinned,
      folder
  )
}