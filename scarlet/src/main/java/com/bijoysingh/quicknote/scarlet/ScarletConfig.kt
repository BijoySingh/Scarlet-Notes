package com.bijoysingh.quicknote.scarlet

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.bijoysingh.quicknote.BuildConfig
import com.bijoysingh.quicknote.firebase.activity.DataPolicyActivity.Companion.openIfNeeded
import com.bijoysingh.quicknote.firebase.support.RemoteConfigFetcher
import com.bijoysingh.quicknote.firebase.support.ScarletAuthenticator
import com.maubis.scarlet.base.auth.IAuthenticator
import com.maubis.scarlet.base.config.IRemoteConfigFetcher
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.note.actions.IFolderActor
import com.maubis.scarlet.base.note.actions.INoteActor
import com.maubis.scarlet.base.note.actions.ITagActor
import com.maubis.scarlet.base.support.Flavor

class ScarletConfig(context: Context) : MaterialNoteConfig(context) {

  override fun authenticator(): IAuthenticator = ScarletAuthenticator()

  override fun noteActions(note: Note): INoteActor = ScarletNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = ScarletTagActor(tag)

  override fun folderActions(folder: Folder): IFolderActor = ScarletFolderActor(folder)

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = RemoteConfigFetcher()

  override fun appFlavor(): Flavor = when (BuildConfig.FLAVOR) {
    "lite" -> Flavor.LITE
    "full" -> Flavor.PRO
    else -> Flavor.NONE
  }

  override fun startListener(activity: AppCompatActivity) {
    openIfNeeded(activity)
  }
}