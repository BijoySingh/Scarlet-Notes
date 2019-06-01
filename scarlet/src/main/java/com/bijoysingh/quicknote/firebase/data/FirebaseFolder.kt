package com.bijoysingh.quicknote.firebase.data

import com.google.firebase.database.Exclude
import com.maubis.scarlet.base.core.folder.IFolderContainer
import java.util.*

// TODO: Remove this on Firebase deprecation
class FirebaseFolder(
    val uuid: String,
    val title: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int) : IFolderContainer {

  @Exclude
  override fun uuid(): String = uuid

  @Exclude
  override fun title(): String = title

  @Exclude
  override fun timestamp(): Long = timestamp

  @Exclude
  override fun updateTimestamp(): Long = updateTimestamp

  @Exclude
  override fun color(): Int = color

  constructor() : this(
      "invalid",
      "",
      Calendar.getInstance().timeInMillis,
      Calendar.getInstance().timeInMillis,
      -0xff8695)
}