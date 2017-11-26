package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.NoteState
import com.github.bijoysingh.starter.util.IntentUtils

class NoteOptionsBottomSheet() : OptionItemBottomSheetBase() {

  var noteFn : () -> Note? = { null }

  override fun setupViewWithDialog(dialog: Dialog) {
    val note = noteFn()
    if (note == null) {
      dismiss()
      return
    }

    setOptions(dialog, getOptions(note))
    setOptionTitle(dialog, R.string.choose_action)
  }

  private fun getOptions(note: Note): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()

    options.add(OptionsItem(
        title = R.string.open_note_night_mode,
        subtitle = R.string.tap_for_action_open_note_night_mode,
        icon = R.drawable.night_mode_white_48dp,
        listener = View.OnClickListener {
          context.startActivity(ViewAdvancedNoteActivity.getIntent(context, note, true))
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.edit_note,
        subtitle = R.string.tap_for_action_edit,
        icon = R.drawable.ic_edit_white_48dp,
        listener = View.OnClickListener {
          note.edit(activity)
          dismiss()
        }
    ))
    // TODO: Add un-favourite option
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.FAVOURITE)
          dismiss()
        }
    ))
    // TODO: Add unarchive option
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.ARCHIVED)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        subtitle = R.string.tap_for_action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          note.share(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        subtitle = R.string.tap_for_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          note.copy(activity)
          dismiss()
        }
    ))
    // TODO: Delete the note in trash mode
    options.add(OptionsItem(
        title = R.string.delete_note,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.deleteItem(note)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.open_in_popup,
        subtitle = R.string.tap_for_action_popup,
        icon = R.drawable.ic_bubble_chart_white_48dp,
        listener = View.OnClickListener {
          note.popup(activity)
          dismiss()
        }
    ))
    return options
  }

  companion object {
    fun openSheet(activity: MainActivity, note: Note) {
      val sheet = NoteOptionsBottomSheet()
      sheet.noteFn = { note }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}