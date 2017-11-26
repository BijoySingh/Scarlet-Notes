package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.NoteState
import com.github.bijoysingh.starter.util.IntentUtils

class HomeNavigationBottomSheet : OptionItemBottomSheetBase() {
  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.nav_sheet_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.nav_home,
        subtitle = R.string.nav_home_details,
        icon = R.drawable.ic_home_white_48dp,
        selected = activity.mode == NoteState.DEFAULT,
        listener = View.OnClickListener {
          activity.onHomeClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_favourites,
        subtitle = R.string.nav_favourites_details,
        icon = R.drawable.ic_favorite_white_48dp,
        selected = activity.mode == NoteState.FAVOURITE,
        listener = View.OnClickListener {
          activity.onFavouritesClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_archived,
        subtitle = R.string.nav_archived_details,
        icon = R.drawable.ic_archive_white_48dp,
        selected = activity.mode == NoteState.ARCHIVED,
        listener = View.OnClickListener {
          activity.onArchivedClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_trash,
        subtitle = R.string.nav_trash_details,
        icon = R.drawable.ic_delete_white_48dp,
        selected = activity.mode == NoteState.TRASH,
        listener = View.OnClickListener {
          activity.onTrashClick();
          dismiss();
        }
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = HomeNavigationBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}