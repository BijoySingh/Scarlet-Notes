package com.bijoysingh.quicknote.database.external

import com.bijoysingh.quicknote.utils.NoteState
import java.util.*

class FirebaseNote(
    val uuid: String,
    val description: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int,
    val state: String,
    val tags: String,
    val locked: Boolean,
    val pinned: Boolean) {

  constructor() : this(
      "invalid",
      "",
      Calendar.getInstance().timeInMillis,
      Calendar.getInstance().timeInMillis,
      -0xff8695,
      NoteState.DEFAULT.name,
      "",
      false,
      false)
}