package com.maubis.scarlet.base.note.actions

import android.app.Dialog
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getNoteState
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.main.sheets.AlertBottomSheet.Companion.openDeleteNotePermanentlySheet
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.folder.sheet.FolderChooseOptionsBottomSheet
import com.maubis.scarlet.base.note.reminders.sheet.ReminderBottomSheet
import com.maubis.scarlet.base.note.selection.activity.KEY_SELECT_EXTRA_MODE
import com.maubis.scarlet.base.note.selection.activity.KEY_SELECT_EXTRA_NOTE_ID
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.note.tag.sheet.TagChooseOptionsBottomSheet
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.settings.sheet.ColorPickerDefaultController
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.Flavor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NoteOptionsBottomSheet() : GridBottomSheetBase() {

  var noteFn: () -> Note? = { null }

  override fun setupViewWithDialog(dialog: Dialog) {
    val note = noteFn()
    if (note === null) {
      dismiss()
      return
    }

    setOptionTitle(dialog, R.string.choose_action)
    setupGrid(dialog, note)
    setupCardViews(note)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setupGrid(dialog: Dialog, note: Note) {
    val gridLayoutIds = arrayOf(
        R.id.quick_actions_properties,
        R.id.note_properties,
        R.id.grid_layout)

    val gridOptionFunctions = arrayOf(
        { noteForAction: Note -> getQuickActions(noteForAction) },
        { noteForAction: Note -> getNotePropertyOptions(noteForAction) },
        { noteForAction: Note -> getOptions(noteForAction) })
    gridOptionFunctions.forEachIndexed { index, function ->
      GlobalScope.launch(Dispatchers.Main) {
        val items = GlobalScope.async(Dispatchers.IO) { function(note) }
        setOptions(dialog.findViewById<GridLayout>(gridLayoutIds[index]), items.await())
      }
    }
  }

  private fun setupCardViews(note: Note) {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity) {
      return
    }

    val tagCardLayout = dialog.findViewById<View>(R.id.tag_card_layout)
    val tags = tagCardLayout.findViewById<TextView>(R.id.tags_content)
    val tagsTitle = tagCardLayout.findViewById<TextView>(R.id.tags_title)
    val tagContent = note.getTagString()
    if (tagContent.isNotBlank()) {
      GlobalScope.launch(Dispatchers.Main) {
        val text = GlobalScope.async(Dispatchers.IO) { Markdown.renderSegment(tagContent, true) }
        tags.visibility = View.VISIBLE
        tagsTitle.visibility = View.GONE
        tags.text = text.await()
      }
    }
    tagCardLayout.setOnClickListener {
      TagChooseOptionsBottomSheet.openSheet(
          activity,
          note
      ) { activity.notifyTagsChanged(note) }
      dismiss()
    }

    val selectCardLayout = dialog.findViewById<View>(R.id.select_notes_layout)
    selectCardLayout.setOnClickListener {
      val intent = Intent(context, SelectNotesActivity::class.java)
      intent.putExtra(KEY_SELECT_EXTRA_MODE, activity.getSelectMode(note))
      intent.putExtra(KEY_SELECT_EXTRA_NOTE_ID, note.uid)
      activity.startActivity(intent)
      dismiss()
    }
    selectCardLayout.visibility = View.VISIBLE
  }

  private fun getQuickActions(note: Note): List<OptionsItem> {
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
    return options
  }

  private fun getNotePropertyOptions(note: Note): List<OptionsItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.choose_note_color,
        subtitle = R.string.tap_for_action_color,
        icon = R.drawable.ic_action_color,
        listener = View.OnClickListener {
          val config = ColorPickerDefaultController(
              title = R.string.choose_note_color,
              colors = listOf(activity.resources.getIntArray(R.array.bright_colors), activity.resources.getIntArray(R.array.bright_colors_accent)),
              selectedColor = note.color,
              onColorSelected = { color ->
                note.color = color
                activity.updateNote(note)
              }
          )
          com.maubis.scarlet.base.support.sheets.openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
          dismiss()
        }
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
    return options
  }

  private fun getOptions(note: Note): List<OptionsItem> {
    val activity = context as ThemedActivity
    if (activity !is INoteOptionSheetActivity) {
      return emptyList()
    }

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = if (note.folder.isBlank()) R.string.folder_option_add_to_notebook else R.string.folder_option_change_notebook,
        subtitle = R.string.folder_option_add_to_notebook,
        icon = R.drawable.ic_folder,
        listener = View.OnClickListener {
          FolderChooseOptionsBottomSheet.openSheet(activity, note, {
            activity.notifyResetOrDismiss()
          })
          dismiss()
        }
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
        title = R.string.reminder,
        subtitle = R.string.reminder,
        icon = R.drawable.ic_action_reminder_icon,
        listener = View.OnClickListener {
          ReminderBottomSheet.openSheet(activity, note)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.duplicate,
        subtitle = R.string.duplicate,
        icon = R.drawable.ic_duplicate,
        listener = View.OnClickListener {
          val copiedNote = NoteBuilder().copy(note)
          copiedNote.uid = null
          copiedNote.uuid = RandomHelper.getRandomString(24)
          copiedNote.save(activity)
          activity.notifyResetOrDismiss()
          dismiss()
        },
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
          if (CoreConfig.instance.appFlavor() == Flavor.PRO) {
            note.viewDistractionFree(activity)
            return@OnClickListener
          }
          com.maubis.scarlet.base.support.sheets.openSheet(activity, InstallProUpsellBottomSheet())
        },
        visible = CoreConfig.instance.appFlavor() != Flavor.NONE,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.open_in_popup,
        subtitle = R.string.tap_for_action_popup,
        icon = R.drawable.ic_bubble_chart_white_48dp,
        listener = View.OnClickListener {
          CoreConfig.instance.noteActions(note).popup(activity)
          dismiss()
        },
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.backup_note_enable,
        subtitle = R.string.backup_note_enable,
        icon = R.drawable.ic_action_backup,
        listener = View.OnClickListener {
          CoreConfig.instance.noteActions(note).enableBackup(activity)
          activity.updateNote(note)
          dismiss()
        },
        visible = note.disableBackup && CoreConfig.instance.appFlavor() != Flavor.NONE,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    options.add(OptionsItem(
        title = R.string.backup_note_disable,
        subtitle = R.string.backup_note_disable,
        icon = R.drawable.ic_action_backup_no,
        listener = View.OnClickListener {
          CoreConfig.instance.noteActions(note).disableBackup(activity)
          activity.updateNote(note)
          dismiss()
        },
        visible = !note.disableBackup && CoreConfig.instance.appFlavor() != Flavor.NONE,
        invalid = activity.lockedContentIsHidden() && note.locked
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_note_options

  override fun getBackgroundCardViewIds(): Array<Int> = emptyArray()

  override fun getOptionsTitleColor(selected: Boolean): Int {
    return ContextCompat.getColor(themedContext(), R.color.light_primary_text)
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = NoteOptionsBottomSheet()
      sheet.noteFn = { note }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}