package com.bijoysingh.quicknote.firebase.data

import com.google.firebase.database.Exclude
import com.maubis.scarlet.base.core.tag.ITagContainer

// TODO: Remove this on Firebase deprecation
class FirebaseTag(
  val uuid: String,
  val title: String) : ITagContainer {

  @Exclude
  override fun title(): String = title

  @Exclude
  override fun uuid(): String = uuid

  constructor() : this("invalid", "")
}