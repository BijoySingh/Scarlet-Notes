package com.maubis.scarlet.base.note.selection.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.sectionPreservingSort
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.folder.sheet.SelectedFolderChooseOptionsBottomSheet
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.note.tag.sheet.SelectedTagChooserBottomSheet
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase

class SelectedNoteOptionsBottomSheet() : GridBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.choose_action)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as SelectNotesActivity
    val options = ArrayList<OptionsItem>()

    val allItemsInTrash = !activity.getAllSelectedNotes().any { it.state !== NoteState.TRASH.name }
    options.add(OptionsItem(
        title = R.string.restore_note,
        subtitle = R.string.tap_for_action_not_trash,
        icon = R.drawable.ic_restore,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
        }),
        visible = allItemsInTrash
    ))

    val allItemsInFavourite = !activity.getAllSelectedNotes().any { it.state !== NoteState.FAVOURITE.name }
    options.add(OptionsItem(
        title = R.string.not_favourite_note,
        subtitle = R.string.tap_for_action_not_favourite,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
        }),
        visible = allItemsInFavourite
    ))
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.FAVOURITE)
          }
        }),
        visible = !allItemsInFavourite
    ))

    val allItemsInArchived = !activity.getAllSelectedNotes().any { it.state !== NoteState.ARCHIVED.name }
    options.add(OptionsItem(
        title = R.string.unarchive_note,
        subtitle = R.string.tap_for_action_not_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
          activity.finish()
        }),
        visible = allItemsInArchived
    ))
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.ARCHIVED)
          }
          activity.finish()
        }),
        visible = !allItemsInArchived
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        subtitle = R.string.tap_for_action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runTextFunction {
            IntentUtils.ShareBuilder(activity)
                .setChooserText(getString(R.string.share_using))
                .setText(it)
                .share()
          }
        })
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        subtitle = R.string.tap_for_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runTextFunction {
            TextUtils.copyToClipboard(activity, it)
          }
          activity.finish()
        })
    ))
    options.add(OptionsItem(
        title = R.string.trash_note,
        subtitle = R.string.tap_for_action_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.mark(activity, NoteState.TRASH)
          }
          activity.finish()
        }),
        visible = !allItemsInTrash
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_permanently,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.delete(activity)
          }
          activity.finish()
        })
    ))
    options.add(OptionsItem(
        title = R.string.change_tags,
        subtitle = R.string.change_tags,
        icon = R.drawable.ic_action_tags,
        listener = lockAwareFunctionRunner(activity) {
          com.maubis.scarlet.base.support.sheets.openSheet(activity, SelectedTagChooserBottomSheet().apply {
            onActionListener = { tag, selectTag ->
              activity.runNoteFunction {
                when (selectTag) {
                  true -> it.addTag(tag)
                  false -> it.removeTag(tag)
                }
                it.save(activity)
              }
            }
          })
        }
    ))
    options.add(OptionsItem(
        title = R.string.folder_option_change_notebook,
        subtitle = R.string.folder_option_change_notebook,
        icon = R.drawable.ic_folder,
        listener = lockAwareFunctionRunner(activity, {
          SelectedFolderChooseOptionsBottomSheet.openSheet(activity, { folder, selectFolder ->
            activity.runNoteFunction {
              when (selectFolder) {
                true -> it.folder = folder.uuid
                false -> it.folder = ""
              }
              it.save(activity)
            }
          })
        })
    ))

    val allLocked = !activity.getAllSelectedNotes().any { !it.locked }
    options.add(OptionsItem(
        title = R.string.lock_note,
        subtitle = R.string.lock_note,
        icon = R.drawable.ic_action_lock,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.locked = true
            it.save(activity)
          }
          activity.finish()
        }),
        visible = !allLocked
    ))
    options.add(OptionsItem(
        title = R.string.unlock_note,
        subtitle = R.string.unlock_note,
        icon = R.drawable.ic_action_unlock,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.locked = false
            it.save(activity)
          }
          activity.finish()
        }),
        visible = allLocked
    ))

    options.add(OptionsItem(
        title = R.string.merge_notes,
        subtitle = R.string.merge_notes,
        icon = R.drawable.ic_merge_note,
        listener = lockAwareFunctionRunner(activity, {
          val selectedNotes = activity.getOrderedSelectedNotes().toMutableList()
          if (selectedNotes.isEmpty()) {
            return@lockAwareFunctionRunner
          }

          val note = selectedNotes.firstOrNull()
          if (note === null) {
            return@lockAwareFunctionRunner
          }

          val formats = note.getFormats().toMutableList()
          selectedNotes.removeAt(0)
          for (noteToAdd in selectedNotes) {
            formats.addAll(noteToAdd.getFormats())
            noteToAdd.delete(activity)
          }
          note.description = FormatBuilder().getDescription(sectionPreservingSort(formats))
          note.save(activity)
          activity.finish()
        })
    ))

    val allBackupDisabled = !activity.getAllSelectedNotes().any { !it.disableBackup }
    options.add(OptionsItem(
        title = R.string.backup_note_enable,
        subtitle = R.string.backup_note_enable,
        icon = R.drawable.ic_action_backup,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.disableBackup = false
            it.save(activity)
          }
          activity.finish()
        }),
        visible = allBackupDisabled
    ))
    options.add(OptionsItem(
        title = R.string.backup_note_disable,
        subtitle = R.string.backup_note_disable,
        icon = R.drawable.ic_action_backup_no,
        listener = lockAwareFunctionRunner(activity, {
          activity.runNoteFunction {
            it.disableBackup = true
            it.save(activity)
          }
          activity.finish()
        }),
        visible = !allBackupDisabled
    ))
    return options
  }

  private fun lockAwareFunctionRunner(
      activity: SelectNotesActivity,
      listener: () -> Unit): View.OnClickListener = View.OnClickListener {
    val hasLockedNote = activity.getAllSelectedNotes().any { it.locked }
    if (!hasLockedNote) {
      listener()
      dismiss()
      return@OnClickListener
    }
    EnterPincodeBottomSheet.openUnlockSheet(
        activity,
        object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
          override fun onSuccess() {
            listener()
            dismiss()
          }
        })
  }

  companion object {
    fun openSheet(activity: SelectNotesActivity) {
      val sheet = SelectedNoteOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}