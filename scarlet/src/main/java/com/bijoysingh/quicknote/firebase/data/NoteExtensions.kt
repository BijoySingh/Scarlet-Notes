package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.core.database.room.note.Note

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
      pinned
  )
}