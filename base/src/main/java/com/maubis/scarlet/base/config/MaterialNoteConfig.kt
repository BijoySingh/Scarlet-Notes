package com.maubis.scarlet.base.config

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.config.auth.NullAuthenticator
import com.maubis.scarlet.base.config.remote.IRemoteConfigFetcher
import com.maubis.scarlet.base.config.remote.NullRemoteConfigFetcher
import com.maubis.scarlet.base.core.folder.IFolderActor
import com.maubis.scarlet.base.core.folder.MaterialFolderActor
import com.maubis.scarlet.base.core.note.INoteActor
import com.maubis.scarlet.base.core.note.MaterialNoteActor
import com.maubis.scarlet.base.core.tag.ITagActor
import com.maubis.scarlet.base.core.tag.MaterialTagActor
import com.maubis.scarlet.base.database.FoldersProvider
import com.maubis.scarlet.base.database.NotesProvider
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.room.AppDatabase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.support.ui.IThemeManager
import com.maubis.scarlet.base.support.ui.ThemeManager
import com.maubis.scarlet.base.support.utils.Flavor
import com.maubis.scarlet.base.support.utils.ImageCache

const val USER_PREFERENCES_STORE_NAME = "USER_PREFERENCES";
const val USER_PREFERENCES_VERSION = 1;

open class MaterialNoteConfig(context: Context) : CoreConfig(context) {
  val db = AppDatabase.createDatabase(context)

  val notesProvider = NotesProvider()
  val tagsProvider = TagsProvider()
  val foldersProvider = FoldersProvider()
  val store = VersionedStore.get(context, USER_PREFERENCES_STORE_NAME, USER_PREFERENCES_VERSION)
  val appTheme = ThemeManager()
  val imageCache = ImageCache(context)

  override fun database(): AppDatabase = db

  override fun authenticator(): IAuthenticator = NullAuthenticator()

  override fun notesDatabase(): NotesProvider = notesProvider

  override fun tagsDatabase(): TagsProvider = tagsProvider

  override fun noteActions(note: Note): INoteActor = MaterialNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = MaterialTagActor(tag)

  override fun foldersDatabase(): FoldersProvider = foldersProvider

  override fun folderActions(folder: Folder): IFolderActor = MaterialFolderActor(folder)

  override fun themeController(): IThemeManager = appTheme

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = NullRemoteConfigFetcher()

  override fun startListener(activity: AppCompatActivity) {}

  override fun appFlavor(): Flavor = Flavor.NONE

  override fun store(): Store = store

  override fun imageCache(): ImageCache = imageCache

  override fun resyncDrive(force: Boolean, onSyncCompleted: () -> Unit) = onSyncCompleted()
}