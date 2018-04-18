package com.bijoysingh.quicknote.utils

import com.bijoysingh.quicknote.activities.external.ExportableTag
import com.maubis.scarlet.base.database.room.tag.Tag
import com.bijoysingh.quicknote.database.external.FirebaseTag
import com.bijoysingh.quicknote.database.utils.saveIfUnique
import com.github.bijoysingh.starter.util.RandomHelper


fun genEmptyTag(): Tag {
  val tag = Tag()
  tag.uid = 0
  tag.title = ""
  tag.uuid = RandomHelper.getRandomString(24)
  return tag
}

fun genEmptyTag(exportableTag: ExportableTag): Tag {
  val tag = genEmptyTag()
  tag.uuid = exportableTag.uuid
  tag.title = exportableTag.title
  return tag
}

fun genFromFirebase(firebaseTag: FirebaseTag): Tag {
  val tag = Tag()
  tag.uuid = firebaseTag.uuid
  tag.title = firebaseTag.title
  return tag
}

fun genImportedTag(exportableTag: ExportableTag): Tag {
  val tag = genEmptyTag(exportableTag)
  tag.saveIfUnique()
  return tag
}
