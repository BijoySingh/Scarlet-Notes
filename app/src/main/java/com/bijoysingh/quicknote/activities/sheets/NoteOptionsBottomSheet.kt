package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.NoteState
import com.bijoysingh.quicknote.utils.NotificationHandler
import com.github.bijoysingh.starter.prefs.DataStore

class NoteOptionsBottomSheet() : GridBottomSheetBase() {

  var noteFn: () -> Note? = { null }

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
    val dataStore = DataStore.get(context)
    val options = ArrayList<OptionsItem>()
    val locked = note.locked && SecurityOptionsBottomSheet.hasPinCodeEnabled(dataStore)
    val notLocked = !note.locked && SecurityOptionsBottomSheet.hasPinCodeEnabled(dataStore)
    options.add(OptionsItem(
        title = R.string.restore_note,
        subtitle = R.string.tap_for_action_not_trash,
        icon = R.drawable.ic_restore,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.noteState == NoteState.TRASH
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
    options.add(OptionsItem(
        title = R.string.not_favourite_note,
        subtitle = R.string.tap_for_action_not_favourite,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.noteState == NoteState.FAVOURITE
    ))
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.FAVOURITE)
          dismiss()
        },
        visible = note.noteState != NoteState.FAVOURITE
    ))
    options.add(OptionsItem(
        title = R.string.unarchive_note,
        subtitle = R.string.tap_for_action_not_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.noteState == NoteState.ARCHIVED
    ))
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.ARCHIVED)
          dismiss()
        },
        visible = note.noteState != NoteState.ARCHIVED
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        subtitle = R.string.tap_for_action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          note.share(activity)
          dismiss()
        },
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        subtitle = R.string.tap_for_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          note.copy(activity)
          dismiss()
        },
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.noteState == NoteState.TRASH,
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.trash_note,
        subtitle = R.string.tap_for_action_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.noteState != NoteState.TRASH,
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.change_tags,
        subtitle = R.string.change_tags,
        icon = R.drawable.ic_action_tags,
        listener = View.OnClickListener {
          TagChooseOptionsBottomSheet.openSheet(activity, note, { activity.setupData() })
          dismiss()
        },
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.choose_note_color,
        subtitle = R.string.tap_for_action_color,
        icon = R.drawable.ic_action_color,
        listener = View.OnClickListener {
          ColorPickerBottomSheet.openSheet(
              activity,
              object : ColorPickerBottomSheet.ColorPickerController {
                override fun onColorSelected(note: Note, color: Int) {
                  note.color = color
                  activity.updateNote(note)
                }

                override fun getNote(): Note {
                  return note
                }
              }
          )
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
        },
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.open_in_notification,
        subtitle = R.string.open_in_notification,
        icon = R.drawable.ic_action_notification,
        listener = View.OnClickListener {
          val handler = NotificationHandler(context, note)
          handler.createNotificationChannel()
          handler.openNotification()
          dismiss()
        },
        invalid = locked
    ))
    options.add(OptionsItem(
        title = R.string.lock_note,
        subtitle = R.string.lock_note,
        icon = R.drawable.ic_action_lock,
        listener = View.OnClickListener {
          note.locked = true
          activity.updateNote(note)
          dismiss()
        },
        visible = notLocked
    ))
    options.add(OptionsItem(
        title = R.string.unlock_note,
        subtitle = R.string.unlock_note,
        icon = R.drawable.ic_action_unlock,
        listener = View.OnClickListener {
          EnterPincodeBottomSheet.openUnlockSheet(
              activity,
              object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
                override fun onSuccess() {
                  note.locked = false
                  activity.updateNote(note)
                  dismiss()
                }
              },
              dataStore)
        },
        visible = locked
    ))
    return options
  }

  companion object {
    fun openSheet(activity: MainActivity, note: Note) {
      val sheet = NoteOptionsBottomSheet()
      sheet.noteFn = { note }
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}