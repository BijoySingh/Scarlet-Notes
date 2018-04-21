package com.maubis.scarlet.base.config

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.Store
import com.maubis.scarlet.base.auth.IAuthenticator
import com.maubis.scarlet.base.core.database.NotesProvider
import com.maubis.scarlet.base.core.database.TagsProvider
import com.maubis.scarlet.base.core.database.room.AppDatabase
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.note.actions.INoteActor
import com.maubis.scarlet.base.note.actions.ITagActor
import com.maubis.scarlet.base.support.ui.IThemeManager

abstract class CoreConfig(context: Context) {

  init {
    Reprint.initialize(context)
  }

  abstract fun database(): AppDatabase

  abstract fun authenticator(): IAuthenticator

  abstract fun notesDatabase(): NotesProvider

  abstract fun tagsDatabase(): TagsProvider

  abstract fun noteActions(note: Note): INoteActor

  abstract fun tagActions(tag: Tag): ITagActor

  abstract fun themeController(): IThemeManager

  abstract fun remoteConfigFetcher(): IRemoteConfigFetcher

  abstract fun startListener(activity: AppCompatActivity)

  abstract fun store(): Store

  companion object {
    lateinit var instance: CoreConfig
  }
}