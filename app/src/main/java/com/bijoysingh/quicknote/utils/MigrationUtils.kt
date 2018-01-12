package com.bijoysingh.quicknote.utils

import android.content.Context
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.RandomHelper
import com.github.bijoysingh.starter.util.TextUtils

const val KEY_MIGRATE_UUID = "KEY_MIGRATE_UUID"

fun migrate(context: Context) {
  val store = DataStore.get(context)
  if (!store.get(KEY_MIGRATE_UUID, false)) {
    val tags = HashMap<Int, Tag>()
    for (tag in Tag.db(context).all) {
      if (TextUtils.isNullOrEmpty(tag.uuid)) {
        tag.uuid = RandomHelper.getRandomString(24)
        tag.save(context)
      }
      tags.put(tag.uid, tag)
    }

    for (note in Note.db(context).all) {
      var saveNote = false
      if (TextUtils.isNullOrEmpty(note.uuid)) {
        note.uuid = RandomHelper.getRandomString(24)
        saveNote = true
      }
      if (!TextUtils.isNullOrEmpty(note.tags)) {
        val tagIDs = note.tagIDs
        note.tags = ""
        for (tagID in tagIDs) {
          val tag = tags.get(tagID)
          if (tag !== null) {
            note.toggleTag(tag)
          }
        }
        saveNote = true
      }
      if (saveNote) {
        note.saveWithoutSync(context)
      }
    }
    store.put(KEY_MIGRATE_UUID, true)
  }
}