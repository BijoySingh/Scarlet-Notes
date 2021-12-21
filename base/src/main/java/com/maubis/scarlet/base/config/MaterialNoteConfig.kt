package com.maubis.scarlet.base.config

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
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
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseState
import com.maubis.scarlet.base.database.room.AppDatabase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag

open class MaterialNoteConfig(context: Context) : CoreConfig() {
  val db = AppDatabase.createDatabase(context)

  val notesProvider = NotesProvider()
  val tagsProvider = TagsProvider()
  val foldersProvider = FoldersProvider()

  override fun database(): AppDatabase = db

  override fun authenticator(): IAuthenticator = NullAuthenticator()

  override fun notesDatabase(): NotesProvider = notesProvider

  override fun tagsDatabase(): TagsProvider = tagsProvider

  override fun noteActions(note: Note): INoteActor = MaterialNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = MaterialTagActor(tag)

  override fun foldersDatabase(): FoldersProvider = foldersProvider

  override fun folderActions(folder: Folder): IFolderActor = MaterialFolderActor(folder)

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = NullRemoteConfigFetcher()

  override fun remoteDatabaseState(): IRemoteDatabaseState {
    return object : IRemoteDatabaseState {
      override fun notifyInsert(data: Any, onExecution: () -> Unit) {}
      override fun notifyRemove(data: Any, onExecution: () -> Unit) {}
    }
  }

  override fun startListener(activity: AppCompatActivity) {}
}