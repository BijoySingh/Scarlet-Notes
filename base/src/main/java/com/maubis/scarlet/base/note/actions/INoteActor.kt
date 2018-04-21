package com.maubis.scarlet.base.note.actions

import android.content.Context
import android.support.v7.app.AppCompatActivity

interface INoteActor {

  fun copy(context: Context)

  fun share(context: Context)

  fun popup(activity: AppCompatActivity)

  fun offlineSave(context: Context)

  fun onlineSave(context: Context)

  fun save(context: Context)

  fun softDelete(context: Context)

  fun offlineDelete(context: Context)

  fun onlineDelete(context: Context)

  fun delete(context: Context)
}