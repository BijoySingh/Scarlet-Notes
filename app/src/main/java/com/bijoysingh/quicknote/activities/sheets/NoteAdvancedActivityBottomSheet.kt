package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.KEY_SELECT_EXTRA_MODE
import com.bijoysingh.quicknote.activities.KEY_SELECT_EXTRA_NOTE_ID
import com.bijoysingh.quicknote.activities.SelectNotesActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.util.RandomHelper

class NoteAdvancedActivityBottomSheet() : GridBottomSheetBase() {

  var noteFn: () -> Note? = { null }
  var isEditMode: Boolean = false

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
    val activity = themedActivity() as ViewAdvancedNoteActivity
    val options = ArrayList<OptionsItem>()
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
          activity.openEditor()
          dismiss()
        },
        visible = !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.not_favourite_note,
        subtitle = R.string.tap_for_action_not_favourite,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.noteState == NoteState.FAVOURITE && !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.FAVOURITE)
          dismiss()
        },
        visible = note.noteState != NoteState.FAVOURITE && !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.unarchive_note,
        subtitle = R.string.tap_for_action_not_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.DEFAULT)
          dismiss()
        },
        visible = note.noteState == NoteState.ARCHIVED && !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.markItem(note, NoteState.ARCHIVED)
          dismiss()
        },
        visible = note.noteState != NoteState.ARCHIVED && !isEditMode
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
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.noteState == NoteState.TRASH && !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.trash_note,
        subtitle = R.string.tap_for_action_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.moveItemToTrashOrDelete(note)
          dismiss()
        },
        visible = note.noteState != NoteState.TRASH && !isEditMode
    ))
    options.add(OptionsItem(
        title = R.string.change_tags,
        subtitle = R.string.change_tags,
        icon = R.drawable.ic_action_tags,
        listener = View.OnClickListener {
          TagChooseOptionsBottomSheet.openSheet(activity, note, { activity.notifyTagsChanged() })
          dismiss()
        }
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
                  note.save(activity)
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
          val intent = Intent(activity, SelectNotesActivity::class.java)
          intent.putExtra(KEY_SELECT_EXTRA_MODE, HomeNavigationState.DEFAULT.name)
          intent.putExtra(KEY_SELECT_EXTRA_NOTE_ID, note.uid)
          activity.startActivity(intent)
          activity.finish()
          dismiss()
        },
        invalid = note.locked
    ))
    options.add(OptionsItem(
        title = R.string.open_in_popup,
        subtitle = R.string.tap_for_action_popup,
        icon = R.drawable.ic_bubble_chart_white_48dp,
        listener = View.OnClickListener {
          note.popup(activity)
          dismiss()
        },
        invalid = note.locked || note.isUnsaved
    ))
    options.add(OptionsItem(
        title = R.string.open_in_notification,
        subtitle = R.string.open_in_notification,
        icon = R.drawable.ic_action_notification,
        listener = View.OnClickListener {
          val handler = NotificationHandler(themedContext(), note)
          handler.createNotificationChannel()
          handler.openNotification()
          dismiss()
        },
        invalid = note.locked || note.isUnsaved
    ))
    options.add(OptionsItem(
        title = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        subtitle = if (note.pinned) R.string.unpin_note else R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = View.OnClickListener {
          note.pinned = !note.pinned
          note.save(activity)
          activity.notifyNoteChange()
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.lock_note,
        subtitle = R.string.lock_note,
        icon = R.drawable.ic_action_lock,
        listener = View.OnClickListener {
          note.locked = true
          note.save(activity)
          activity.notifyNoteChange()
          dismiss()
        },
        visible = !note.locked
    ))
    options.add(OptionsItem(
        title = R.string.unlock_note,
        subtitle = R.string.unlock_note,
        icon = R.drawable.ic_action_unlock,
        listener = View.OnClickListener {
          note.locked = false
          note.save(activity)
          activity.notifyNoteChange()
          dismiss()
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
          activity.finish()
          dismiss()
        },
        invalid = note.locked
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteNotePermanentlySheet(activity, note, { activity.finish() })
          dismiss()
        },
        visible = note.noteState !== NoteState.TRASH,
        invalid = note.locked
    ))
    options.add(OptionsItem(
        title = R.string.voice_action_title,
        subtitle = R.string.voice_action_title,
        icon = R.drawable.ic_action_speak_aloud,
        listener = View.OnClickListener {
          TextToSpeechBottomSheet.openSheet(activity, note)
          dismiss()
        },
        invalid = note.locked
    ))
    options.add(OptionsItem(
        title = R.string.reminder,
        subtitle = R.string.reminder,
        icon = R.drawable.ic_action_reminder,
        listener = View.OnClickListener {
          if (getAppFlavor() == Flavor.PRO) {
            ReminderBottomSheet.openSheet(activity, note)
            dismiss()
            return@OnClickListener
          }
        },
        visible = getAppFlavor() != Flavor.NONE,
        invalid = note.locked
    ))
    return options
  }

  companion object {
    fun openSheet(activity: ViewAdvancedNoteActivity,
                  note: Note,
                  isEditMode: Boolean) {
      val sheet = NoteAdvancedActivityBottomSheet()
      sheet.noteFn = { note }
      sheet.isEditMode = isEditMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}