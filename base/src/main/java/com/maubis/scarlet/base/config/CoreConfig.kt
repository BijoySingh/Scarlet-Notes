package com.maubis.scarlet.base.config

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.Store
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.MarkdownConfig.Companion.config
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.config.remote.IRemoteConfigFetcher
import com.maubis.scarlet.base.core.folder.IFolderActor
import com.maubis.scarlet.base.core.note.INoteActor
import com.maubis.scarlet.base.core.tag.ITagActor
import com.maubis.scarlet.base.database.FoldersProvider
import com.maubis.scarlet.base.database.NotesProvider
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.room.AppDatabase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.support.ui.IThemeManager
import com.maubis.scarlet.base.support.utils.Flavor
import com.maubis.scarlet.base.support.utils.ImageCache

abstract class CoreConfig(context: Context) {

  init {
    Reprint.initialize(context)
    config.spanConfig.headingTypeface = ResourcesCompat.getFont(context, R.font.monserrat) ?: Typeface.DEFAULT
    FONT_MONSERRAT = config.spanConfig.headingTypeface
    FONT_OPEN_SANS = ResourcesCompat.getFont(context, R.font.open_sans) ?: Typeface.DEFAULT
  }

  abstract fun database(): AppDatabase

  abstract fun authenticator(): IAuthenticator

  abstract fun notesDatabase(): NotesProvider

  abstract fun tagsDatabase(): TagsProvider

  abstract fun foldersDatabase(): FoldersProvider

  abstract fun noteActions(note: Note): INoteActor

  abstract fun tagActions(tag: Tag): ITagActor

  abstract fun folderActions(folder: Folder): IFolderActor

  abstract fun themeController(): IThemeManager

  abstract fun remoteConfigFetcher(): IRemoteConfigFetcher

  abstract fun startListener(activity: AppCompatActivity)

  abstract fun appFlavor(): Flavor

  abstract fun store(): Store

  abstract fun externalFolderSync(): FolderRemoteDatabase

  abstract fun imageCache(): ImageCache

  companion object {
    lateinit var instance: CoreConfig
    val notesDb get() = instance.notesDatabase()
    val tagsDb get() = instance.tagsDatabase()
    val foldersDb get() = instance.foldersDatabase()

    var FONT_MONSERRAT: Typeface = Typeface.DEFAULT
    var FONT_OPEN_SANS: Typeface = Typeface.DEFAULT
  }
}