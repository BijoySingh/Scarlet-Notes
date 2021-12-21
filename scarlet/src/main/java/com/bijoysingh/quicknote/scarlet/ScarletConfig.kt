package com.bijoysingh.quicknote.scarlet

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.bijoysingh.quicknote.Scarlet.Companion.remoteDatabaseStateController
import com.bijoysingh.quicknote.firebase.activity.DataPolicyActivity.Companion.openIfNeeded
import com.bijoysingh.quicknote.firebase.support.RemoteConfigFetcher
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.config.remote.IRemoteConfigFetcher
import com.maubis.scarlet.base.core.folder.IFolderActor
import com.maubis.scarlet.base.core.note.INoteActor
import com.maubis.scarlet.base.core.tag.ITagActor
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseState
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag

class ScarletConfig(context: Context) : MaterialNoteConfig(context) {

  override fun authenticator(): IAuthenticator = ScarletAuthenticator()

  override fun noteActions(note: Note): INoteActor = ScarletNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = ScarletTagActor(tag)

  override fun folderActions(folder: Folder): IFolderActor = ScarletFolderActor(folder)

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = RemoteConfigFetcher()

  override fun remoteDatabaseState(): IRemoteDatabaseState = remoteDatabaseStateController!!

  override fun startListener(activity: AppCompatActivity) {
    openIfNeeded(activity)
  }
}