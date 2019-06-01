package com.bijoysingh.quicknote.firebase.data

import com.google.firebase.database.Exclude
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteState
import java.util.*

// TODO: Remove this on Firebase deprecation
class FirebaseNote(
    val uuid: String,
    val description: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int,
    val state: String,
    val tags: String,
    val locked: Boolean,
    val pinned: Boolean,
    val folder: String) : INoteContainer {

  @Exclude
  override fun uuid(): String = uuid

  @Exclude
  override fun description(): String = description

  @Exclude
  override fun timestamp(): Long = timestamp

  @Exclude
  override fun updateTimestamp(): Long = updateTimestamp

  @Exclude
  override fun color(): Int = color

  @Exclude
  override fun state(): String = state

  @Exclude
  override fun tags(): String = tags

  @Exclude
  override fun meta(): Map<String, Any> = emptyMap()

  @Exclude
  override fun locked(): Boolean = locked

  @Exclude
  override fun pinned(): Boolean = pinned

  @Exclude
  override fun folder(): String = folder

  constructor() : this(
      "invalid",
      "",
      Calendar.getInstance().timeInMillis,
      Calendar.getInstance().timeInMillis,
      -0xff8695,
      NoteState.DEFAULT.name,
      "",
      false,
      false,
      "")
}