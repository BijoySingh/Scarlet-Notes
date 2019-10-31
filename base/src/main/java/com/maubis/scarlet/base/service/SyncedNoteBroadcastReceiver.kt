package com.maubis.scarlet.base.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.bijoysingh.starter.util.TextUtils

const val KEY_UUID = "KEY_UUID"

enum class NoteBroadcast {
  NOTE_CHANGED,
  NOTE_DELETED,
  TAG_CHANGED,
  TAG_DELETED,
  FOLDER_CHANGED,
  FOLDER_DELETED,
}

fun getNoteIntentFilter(): IntentFilter {
  val filter = IntentFilter()
  filter.addAction(NoteBroadcast.NOTE_CHANGED.name)
  filter.addAction(NoteBroadcast.NOTE_DELETED.name)
  filter.addAction(NoteBroadcast.TAG_CHANGED.name)
  filter.addAction(NoteBroadcast.TAG_DELETED.name)
  filter.addAction(NoteBroadcast.FOLDER_CHANGED.name)
  filter.addAction(NoteBroadcast.FOLDER_DELETED.name)
  return filter
}

fun sendNoteBroadcast(
  context: Context,
  broadcast: NoteBroadcast,
  uuid: String) {
  val intent = Intent()
  intent.action = broadcast.name
  intent.putExtra(KEY_UUID, uuid)
  context.sendBroadcast(intent)
}

class SyncedNoteBroadcastReceiver(val listener: () -> Unit) : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    val uuid = intent.getStringExtra(KEY_UUID)
    if (TextUtils.isNullOrEmpty(uuid)) {
      return
    }

    val action = intent.getAction()
    when (action) {
      NoteBroadcast.NOTE_CHANGED.name -> listener()
      NoteBroadcast.NOTE_DELETED.name -> listener()
      NoteBroadcast.TAG_CHANGED.name -> listener()
      NoteBroadcast.TAG_DELETED.name -> listener()
      NoteBroadcast.FOLDER_CHANGED.name -> listener()
      NoteBroadcast.FOLDER_DELETED.name -> listener()
    }
  }
}