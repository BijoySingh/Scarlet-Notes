package com.maubis.scarlet.base.note.selection.sheet

import android.app.Dialog
import android.support.v4.content.ContextCompat
import com.facebook.litho.ComponentContext
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.sectionPreservingSort
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.main.sheets.AlertBottomSheet
import com.maubis.scarlet.base.main.sheets.AlertSheetConfig
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.folder.sheet.SelectedFolderChooseOptionsBottomSheet
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.note.tag.sheet.SelectedTagChooserBottomSheet
import com.maubis.scarlet.base.security.sheets.openUnlockSheet
import com.maubis.scarlet.base.support.sheets.GridOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.GridSectionItem
import com.maubis.scarlet.base.support.specs.GridSectionOptionItem

class SelectedNotesOptionsBottomSheet : GridOptionBottomSheet() {
  override fun title(): Int = R.string.choose_action

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem> {
    val options = ArrayList<GridSectionItem>()
    options.add(getQuickActions(componentContext, dialog))
    options.add(getSecondaryActions(componentContext, dialog))
    options.add(getTertiaryActions(componentContext, dialog))
    return options
  }

  private fun getQuickActions(componentContext: ComponentContext, dialog: Dialog): GridSectionItem {
    val activity = componentContext.androidContext as SelectNotesActivity
    val options = ArrayList<GridSectionOptionItem>()

    val allItemsInTrash = !activity.getAllSelectedNotes().any { it.state !== NoteState.TRASH.name }
    options.add(GridSectionOptionItem(
        label = R.string.restore_note,
        icon = R.drawable.ic_restore,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
          activity.finish()
        },
        visible = allItemsInTrash
    ))

    val allItemsInFavourite = !activity.getAllSelectedNotes().any { it.state !== NoteState.FAVOURITE.name }
    options.add(GridSectionOptionItem(
        label = R.string.not_favourite_note,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
          activity.finish()
        },
        visible = allItemsInFavourite
    ))
    options.add(GridSectionOptionItem(
        label = R.string.favourite_note,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.FAVOURITE)
          }
          activity.finish()
        },
        visible = !allItemsInFavourite
    ))

    val allItemsInArchived = !activity.getAllSelectedNotes().any { it.state !== NoteState.ARCHIVED.name }
    options.add(GridSectionOptionItem(
        label = R.string.unarchive_note,
        icon = R.drawable.ic_archive_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.DEFAULT)
          }
          activity.finish()
        },
        visible = allItemsInArchived
    ))
    options.add(GridSectionOptionItem(
        label = R.string.archive_note,
        icon = R.drawable.ic_archive_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.ARCHIVED)
          }
          activity.finish()
        },
        visible = !allItemsInArchived
    ))
    options.add(GridSectionOptionItem(
        label = R.string.send_note,
        icon = R.drawable.ic_share_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runTextFunction {
            IntentUtils.ShareBuilder(activity)
                .setChooserText(getString(R.string.share_using))
                .setText(it)
                .share()
          }
        }
    ))
    options.add(GridSectionOptionItem(
        label = R.string.copy_note,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runTextFunction {
            TextUtils.copyToClipboard(activity, it)
          }
          activity.finish()
        }
    ))
    options.add(GridSectionOptionItem(
        label = R.string.trash_note,
        icon = R.drawable.ic_delete_white_48dp,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.mark(activity, NoteState.TRASH)
          }
          activity.finish()
        },
        visible = !allItemsInTrash
    ))

    return GridSectionItem(
        options = options,
        sectionColor = ContextCompat.getColor(activity, R.color.material_blue_800))
  }

  private fun getSecondaryActions(componentContext: ComponentContext, dialog: Dialog): GridSectionItem {
    val activity = componentContext.androidContext as SelectNotesActivity
    val options = ArrayList<GridSectionOptionItem>()

    options.add(GridSectionOptionItem(
        label = R.string.change_tags,
        icon = R.drawable.ic_action_tags,
        listener = lockAwareFunctionRunner(activity) {
          openSheet(activity, SelectedTagChooserBottomSheet().apply {
            onActionListener = { tag, selectTag ->
              activity.runNoteFunction {
                when (selectTag) {
                  true -> it.addTag(tag)
                  false -> it.removeTag(tag)
                }
                it.save(activity)
              }
              activity.finish()
            }
          })
        }
    ))

    val allItemsPinned = !activity.getAllSelectedNotes().any { !it.pinned }
    options.add(GridSectionOptionItem(
        label = R.string.pin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.pinned = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allItemsPinned
    ))
    options.add(GridSectionOptionItem(
        label = R.string.unpin_note,
        icon = R.drawable.ic_pin,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.pinned = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allItemsPinned
    ))

    val allLocked = !activity.getAllSelectedNotes().any { !it.locked }
    options.add(GridSectionOptionItem(
        label = R.string.lock_note,
        icon = R.drawable.ic_action_lock,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.locked = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allLocked
    ))
    options.add(GridSectionOptionItem(
        label = R.string.unlock_note,
        icon = R.drawable.ic_action_unlock,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.locked = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allLocked
    ))

    return GridSectionItem(
        options = options,
        sectionColor = ContextCompat.getColor(activity, R.color.material_red_800))
  }

  private fun getTertiaryActions(componentContext: ComponentContext, dialog: Dialog): GridSectionItem {
    val activity = componentContext.androidContext as SelectNotesActivity
    val options = ArrayList<GridSectionOptionItem>()


    options.add(GridSectionOptionItem(
        label = R.string.folder_option_change_notebook,
        icon = R.drawable.ic_folder,
        listener = lockAwareFunctionRunner(activity) {
          SelectedFolderChooseOptionsBottomSheet.openSheet(activity) { folder, selectFolder ->
            activity.runNoteFunction {
              when (selectFolder) {
                true -> it.folder = folder.uuid
                false -> it.folder = ""
              }
              it.save(activity)
            }
            activity.finish()
          }
        }
    ))

    options.add(GridSectionOptionItem(
        label = R.string.merge_notes,
        icon = R.drawable.ic_merge_note,
        listener = lockAwareFunctionRunner(activity) {
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
        }
    ))

    options.add(GridSectionOptionItem(
        label = R.string.delete_note_permanently,
        icon = R.drawable.ic_delete_permanently,
        listener = lockAwareFunctionRunner(activity) {
          openSheet(activity, AlertBottomSheet().apply {
            config = AlertSheetConfig(
                description = R.string.delete_sheet_delete_selected_notes_permanently,
                onPositiveClick = {
                  activity.runNoteFunction {
                    it.delete(activity)
                  }
                  activity.finish()
                }
            )
          })
        }
    ))

    val allBackupDisabled = !activity.getAllSelectedNotes().any { !it.disableBackup }
    options.add(GridSectionOptionItem(
        label = R.string.backup_note_enable,
        icon = R.drawable.ic_action_backup,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.disableBackup = false
            it.save(activity)
          }
          activity.finish()
        },
        visible = allBackupDisabled
    ))
    options.add(GridSectionOptionItem(
        label = R.string.backup_note_disable,
        icon = R.drawable.ic_action_backup_no,
        listener = lockAwareFunctionRunner(activity) {
          activity.runNoteFunction {
            it.disableBackup = true
            it.save(activity)
          }
          activity.finish()
        },
        visible = !allBackupDisabled
    ))
    return GridSectionItem(
        options = options,
        sectionColor = ContextCompat.getColor(activity, R.color.material_teal_800))
  }

  private fun lockAwareFunctionRunner(
      activity: SelectNotesActivity,
      listener: () -> Unit): () -> Unit = {
    val hasLockedNote = activity.getAllSelectedNotes().any { it.locked }
    when {
      hasLockedNote -> openUnlockSheet(
          activity = activity,
          onUnlockSuccess = {
            listener()
            dismiss()
          },
          onUnlockFailure = {})
      else -> {
        listener()
        dismiss()
      }
    }
  }
}