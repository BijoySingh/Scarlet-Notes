package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.view.View
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.main.sheets.AlertBottomSheet
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.folder.delete
import com.maubis.scarlet.base.note.tag.delete
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemBottomSheetBase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class DeleteAndMoreOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    launch(UI) {
      val options = async(CommonPool) { getOptions() }
      setOptions(dialog, options.await())
    }
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_notes,
        subtitle = R.string.home_option_delete_all_notes_details,
        icon = R.drawable.ic_note_white_48dp,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_notes_details) {
            launch(UI) {
              withContext(CommonPool) { notesDb.getAll().forEach { it.delete(activity) } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_tags,
        subtitle = R.string.home_option_delete_all_tags_details,
        icon = R.drawable.ic_action_tags,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_tags_details) {
            launch(UI) {
              withContext(CommonPool) { tagsDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_folders,
        subtitle = R.string.home_option_delete_all_folders_details,
        icon = R.drawable.ic_folder,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_folders_details) {
            launch(UI) {
              withContext(CommonPool) { foldersDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_everything,
        subtitle = R.string.home_option_delete_everything_details,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_everything_details) {
            launch(UI) {
              val notes = async(CommonPool) { notesDb.getAll().forEach { it.delete(activity) } }
              val tags = async(CommonPool) { tagsDb.getAll().forEach { it.delete() } }
              val folders = async(CommonPool) { foldersDb.getAll().forEach { it.delete() } }

              notes.await()
              tags.await()
              folders.await()

              activity.resetAndSetupData()
              dismiss()
            }
          }

        }
    ))
    val forgetMeClick = CoreConfig.instance.authenticator().openForgetMeActivity(activity)
    options.add(OptionsItem(
        title = R.string.forget_me_option_title,
        subtitle = R.string.forget_me_option_details,
        icon = R.drawable.ic_action_forget_me,
        listener = View.OnClickListener {
          forgetMeClick?.run()
          dismiss()
        },
        visible = forgetMeClick !== null && CoreConfig.instance.authenticator().isLoggedIn()
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = DeleteAndMoreOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}