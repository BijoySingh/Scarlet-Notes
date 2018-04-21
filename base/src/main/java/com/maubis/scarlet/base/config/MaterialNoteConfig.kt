package com.maubis.scarlet.base.config

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.maubis.scarlet.base.auth.IAuthenticator
import com.maubis.scarlet.base.auth.NullAuthenticator
import com.maubis.scarlet.base.core.database.NotesProvider
import com.maubis.scarlet.base.core.database.TagsProvider
import com.maubis.scarlet.base.core.database.room.AppDatabase
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.note.actions.INoteActor
import com.maubis.scarlet.base.note.actions.ITagActor
import com.maubis.scarlet.base.note.actions.MaterialNoteActor
import com.maubis.scarlet.base.note.actions.MaterialTagActor
import com.maubis.scarlet.base.support.database.NotesDB
import com.maubis.scarlet.base.support.database.TagsDB
import com.maubis.scarlet.base.support.ui.IThemeManager
import com.maubis.scarlet.base.support.ui.ThemeManager

const val USER_PREFERENCES_STORE_NAME = "USER_PREFERENCES";
const val USER_PREFERENCES_VERSION = 1;

open class MaterialNoteConfig(context: Context) : CoreConfig(context) {
  val db = AppDatabase.createDatabase(context)

  val notesDB = NotesDB()
  val tagsDB = TagsDB()
  val store = VersionedStore.get(context, USER_PREFERENCES_STORE_NAME, USER_PREFERENCES_VERSION)
  val appTheme = ThemeManager()

  init {
    // Temporary object, will be removed soon
    DataStore.get(context)?.migrateToStore(store)
  }

  override fun database(): AppDatabase = db

  override fun authenticator(): IAuthenticator = NullAuthenticator()

  override fun notesDatabase(): NotesProvider = notesDB

  override fun tagsDatabase(): TagsProvider = tagsDB

  override fun noteActions(note: Note): INoteActor = MaterialNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = MaterialTagActor(tag)

  override fun themeController(): IThemeManager = appTheme

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = NullRemoteConfigFetcher()

  override fun startListener(activity: AppCompatActivity) {}

  override fun store(): Store = store

}