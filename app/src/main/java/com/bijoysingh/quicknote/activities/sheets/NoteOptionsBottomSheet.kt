package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.*
import com.bijoysingh.quicknote.activities.sheets.AlertBottomSheet.Companion.openDeleteNotePermanentlySheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.*
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.util.RandomHelper

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
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.restore_note,
        subtitle = R.string.tap_for_action_not_trash,
        icon = R.drawable.ic_restore,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.getNoteState() == NoteState.TRASH
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
        visible = note.getNoteState() == NoteState.FAVOURITE
    ))
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.FAVOURITE)
          dismiss()
        },
        visible = note.getNoteState() != NoteState.FAVOURITE
    ))
    options.add(OptionsItem(
        title = R.string.unarchive_note,
        subtitle = R.string.tap_for_action_not_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.getNoteState() == NoteState.ARCHIVED
    ))
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.ARCHIVED)
          dismiss()
        },
        visible = note.getNoteState() != NoteState.ARCHIVED
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        subtitle = R.string.tap_for_action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          note.share(activity)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        subtitle = R.string.tap_for_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          note.copy(activity)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.getNoteState() == NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.trash_note,
        subtitle = R.string.tap_for_action_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.getNoteState() != NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.change_tags,
        subtitle = R.string.change_tags,
        icon = R.drawable.ic_action_tags,
        listener = View.OnClickListener {
          TagChooseOptionsBottomSheet.openSheet(
              activity,
              note,
              { activity.notifyTagsChanged(note) })
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
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
        title = R.string.select,
        subtitle = R.string.select,
        icon = R.drawable.ic_action_select,
        listener = View.OnClickListener {
          val intent = Intent(context, SelectNotesActivity::class.java)
          intent.putExtra(KEY_SELECT_EXTRA_MODE, activity.getSelectMode(note))
          intent.putExtra(KEY_SELECT_EXTRA_NOTE_ID, note.uid)
          activity.startActivity(intent)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.open_in_popup,
        subtitle = R.string.tap_for_action_popup,
        icon = R.drawable.ic_bubble_chart_white_48dp,
        listener = View.OnClickListener {
          note.popup(activity)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.open_in_notification,
        subtitle = R.string.open_in_notification,
        icon = R.drawable.ic_action_notification,
        listener = View.OnClickListener {
          val handler = NotificationHandler(themedContext())
          handler.openNotification(NotificationConfig(note = note))
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        subtitle = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = View.OnClickListener {
          note.pinned = !note.pinned
          activity.updateNote(note)
          dismiss()
        }
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
        visible = !note.locked
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
              })
        },
        visible = note.locked
    ))
    options.add(OptionsItem(
        title = R.string.duplicate,
        subtitle = R.string.duplicate,
        icon = R.drawable.ic_duplicate,
        listener = View.OnClickListener {
          val copiedNote = copyNote(note)
          copiedNote.uid = null
          copiedNote.uuid = RandomHelper.getRandomString(24)
          copiedNote.save(activity)
          activity.notifyResetOrDismiss()
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          openDeleteNotePermanentlySheet(activity, note, { activity.notifyResetOrDismiss() })
          dismiss()
        },
        visible = note.getNoteState() !== NoteState.TRASH,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.voice_action_title,
        subtitle = R.string.voice_action_title,
        icon = R.drawable.ic_action_speak_aloud,
        listener = View.OnClickListener {
          TextToSpeechBottomSheet.openSheet(activity, note)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.view_distraction_free,
        subtitle = R.string.view_distraction_free,
        icon = R.drawable.ic_action_distraction_free,
        listener = View.OnClickListener {
          if (getAppFlavor() == Flavor.PRO) {
            note.viewDistractionFree(activity)
            return@OnClickListener
          }
        },
        visible = getAppFlavor() != Flavor.NONE,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.reminder,
        subtitle = R.string.reminder,
        icon = R.drawable.ic_action_reminder_icon,
        listener = View.OnClickListener {
          if (getAppFlavor() == Flavor.PRO) {
            ReminderBottomSheet.openSheet(activity, note)
            dismiss()
            return@OnClickListener
          }
        },
        visible = getAppFlavor() != Flavor.NONE,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = NoteOptionsBottomSheet()
      sheet.noteFn = { note }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}