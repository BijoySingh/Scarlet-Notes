package com.maubis.scarlet.base.note.selection.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase

class SelectedNoteOptionsBottomSheet() : GridBottomSheetBase() {

  var mode = ""

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.choose_action)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as SelectNotesActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.restore_note,
        subtitle = R.string.tap_for_action_not_trash,
        icon = R.drawable.ic_restore,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, if (mode == HomeNavigationState.TRASH.name) NoteState.DEFAULT else NoteState.TRASH)
          }
        },
        visible = mode == NoteState.TRASH.name
    ))
    options.add(OptionsItem(
        title = R.string.not_favourite_note,
        subtitle = R.string.tap_for_action_not_favourite,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, if (mode == HomeNavigationState.FAVOURITE.name) NoteState.DEFAULT else NoteState.FAVOURITE)
          }
        },
        visible = mode == NoteState.FAVOURITE.name
    ))
    options.add(OptionsItem(
        title = R.string.favourite_note,
        subtitle = R.string.tap_for_action_favourite,
        icon = R.drawable.ic_favorite_border_white_48dp,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, if (mode == HomeNavigationState.FAVOURITE.name) NoteState.DEFAULT else NoteState.FAVOURITE)
          }
        },
        visible = mode != NoteState.FAVOURITE.name
    ))
    options.add(OptionsItem(
        title = R.string.unarchive_note,
        subtitle = R.string.tap_for_action_not_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, if (mode == HomeNavigationState.ARCHIVED.name) NoteState.DEFAULT else NoteState.ARCHIVED)
            activity.finish()
          }
        },
        visible = mode == NoteState.ARCHIVED.name
    ))
    options.add(OptionsItem(
        title = R.string.archive_note,
        subtitle = R.string.tap_for_action_archive,
        icon = R.drawable.ic_archive_white_48dp,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, if (mode == HomeNavigationState.ARCHIVED.name) NoteState.DEFAULT else NoteState.ARCHIVED)
            activity.finish()
          }
        },
        visible = mode != NoteState.ARCHIVED.name
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        subtitle = R.string.tap_for_action_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          activity.runTextFunction {
            IntentUtils.ShareBuilder(activity)
                .setChooserText(getString(R.string.share_using))
                .setText(it)
                .share()
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        subtitle = R.string.tap_for_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          activity.runTextFunction {
            TextUtils.copyToClipboard(activity, it)
            activity.finish()
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.trash_note,
        subtitle = R.string.tap_for_action_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.mark(activity, NoteState.TRASH)
            activity.finish()
          }
        },
        visible = mode != NoteState.TRASH.name
    ))
    options.add(OptionsItem(
        title = R.string.delete_note_permanently,
        subtitle = R.string.tap_for_action_delete,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          activity.runNoteFunction {
            it.delete(activity)
            activity.finish()
          }
        }
    ))
    return options
  }

  companion object {
    fun openSheet(activity: SelectNotesActivity, mode: String) {
      val sheet = SelectedNoteOptionsBottomSheet()
      sheet.mode = mode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}