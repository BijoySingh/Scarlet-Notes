package com.bijoysingh.quicknote.database.external

import com.bijoysingh.quicknote.utils.NoteState
import java.util.*

class FirebaseTag(
    val uuid: String,
    val title: String) {

  constructor() : this(
      "invalid",
      "")
}