package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.main.sheets.openDeleteAllXSheet
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.folder.delete
import com.maubis.scarlet.base.note.tag.delete
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import kotlinx.coroutines.*

class DeleteAndMoreOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_delete_notes_and_more

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.home_option_delete_all_notes,
        subtitle = R.string.home_option_delete_all_notes_details,
        icon = R.drawable.ic_note_white_48dp,
        listener = {
          openDeleteAllXSheet(activity, R.string.home_option_delete_all_notes_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { notesDb.getAll().forEach { it.delete(activity) } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_delete_all_tags,
        subtitle = R.string.home_option_delete_all_tags_details,
        icon = R.drawable.ic_action_tags,
        listener = {
          openDeleteAllXSheet(activity, R.string.home_option_delete_all_tags_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { tagsDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_delete_all_folders,
        subtitle = R.string.home_option_delete_all_folders_details,
        icon = R.drawable.ic_folder,
        listener = {
          openDeleteAllXSheet(activity, R.string.home_option_delete_all_folders_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { foldersDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_delete_everything,
        subtitle = R.string.home_option_delete_everything_details,
        icon = R.drawable.ic_delete_permanently,
        listener = {
          openDeleteAllXSheet(activity, R.string.home_option_delete_everything_details) {
            GlobalScope.launch(Dispatchers.Main) {
              val notes = GlobalScope.async(Dispatchers.IO) { notesDb.getAll().forEach { it.delete(activity) } }
              val tags = GlobalScope.async(Dispatchers.IO) { tagsDb.getAll().forEach { it.delete() } }
              val folders = GlobalScope.async(Dispatchers.IO) { foldersDb.getAll().forEach { it.delete() } }

              notes.await()
              tags.await()
              folders.await()

              activity.resetAndSetupData()
              dismiss()
            }
          }

        }
    ))
    val forgetMeClick = ApplicationBase.instance.authenticator().openForgetMeActivity(activity)
    options.add(LithoOptionsItem(
        title = R.string.forget_me_option_title,
        subtitle = R.string.forget_me_option_details,
        icon = R.drawable.ic_action_forget_me,
        listener = {
          forgetMeClick?.run()
          dismiss()
        },
        visible = forgetMeClick !== null && ApplicationBase.instance.authenticator().isLegacyLoggedIn()
    ))
    return options
  }
}