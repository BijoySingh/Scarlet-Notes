package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.widget.TextView
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment

class AlertBottomSheet : ThemedBottomSheetFragment() {

  var listener: AlertDetails? = null
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val details = listener
    if (details === null) {
      return
    }

    val sheetTitle = dialog.findViewById<TextView>(R.id.alert_title)
    val sheetDescription = dialog.findViewById<TextView>(R.id.alert_description)
    val sheetYes = dialog.findViewById<TextView>(R.id.alert_yes)
    val sheetNo = dialog.findViewById<TextView>(R.id.alert_no)

    sheetTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    sheetDescription.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))

    sheetTitle.setText(details.getTitle())
    sheetDescription.setText(details.getDescription())
    sheetYes.setText(details.getPositiveText())
    sheetNo.setText(details.getNegativeText())

    sheetYes.setOnClickListener {
      details.getPositiveClickListener()
      dismiss()
    }

    sheetNo.setOnClickListener {
      details.getNegativeClickListener()
      dismiss()
    }

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_alert

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.alert_card)

  companion object {
    const val IMAGE_SYNC_NOTICE = "IMAGE_SYNC_NOTICE"

    fun openSheet(activity: ThemedActivity, listener: AlertDetails) {
      val sheet = AlertBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun openDeleteTrashSheet(activity: MainActivity) {
      val details = object : AlertDetails {
        override fun getTitle(): Int = R.string.delete_sheet_are_you_sure

        override fun getDescription(): Int = R.string.delete_sheet_delete_trash

        override fun getPositiveText(): Int = R.string.delete_sheet_delete_trash_yes

        override fun getNegativeText(): Int = R.string.delete_sheet_delete_trash_no

        override fun getPositiveClickListener() {
          val notes = notesDB.getByNoteState(arrayOf(NoteState.TRASH.name))
          for (note in notes) {
            note.delete(activity)
          }
          activity.setupData()
        }

        override fun getNegativeClickListener() {
          // Ignore, nothing needs to happen
        }
      }
      openSheet(activity, details)
    }

    fun openDeleteNotePermanentlySheet(activity: ThemedActivity, note: Note, onDelete: () -> Unit) {
      val details = object : AlertDetails {
        override fun getTitle(): Int = R.string.delete_sheet_are_you_sure

        override fun getDescription(): Int = R.string.delete_sheet_delete_note_permanently

        override fun getPositiveText(): Int = R.string.delete_sheet_delete_trash_yes

        override fun getNegativeText(): Int = R.string.delete_sheet_delete_trash_no

        override fun getPositiveClickListener() {
          note.delete(activity)
          onDelete()
        }

        override fun getNegativeClickListener() {
          // Ignore, nothing needs to happen
        }
      }
      openSheet(activity, details)
    }

    fun openDeleteFormatDialog(activity: ViewAdvancedNoteActivity, format: Format) {
      val details = object : AlertDetails {
        override fun getTitle(): Int = R.string.delete_sheet_are_you_sure

        override fun getDescription(): Int = R.string.image_delete_all_devices

        override fun getPositiveText(): Int = R.string.delete_sheet_delete_trash_yes

        override fun getNegativeText(): Int = R.string.delete_sheet_delete_trash_no

        override fun getPositiveClickListener() {
          activity.deleteFormat(format)
        }

        override fun getNegativeClickListener() {
          // Ignore, nothing needs to happen
        }
      }
      openSheet(activity, details)
    }

    fun openImageNotSynced(activity: ThemedActivity) {
      val details = object : AlertDetails {
        override fun getTitle(): Int = R.string.image_not_uploaded

        override fun getDescription(): Int = R.string.image_not_uploaded_details

        override fun getPositiveText(): Int = R.string.image_not_uploaded_i_understand

        override fun getNegativeText(): Int = R.string.delete_sheet_delete_trash_no

        override fun getPositiveClickListener() {
          CoreConfig.instance.store().put(IMAGE_SYNC_NOTICE, 1)
        }

        override fun getNegativeClickListener() {
          // Ignore, nothing needs to happen
        }
      }
      openSheet(activity, details)
    }
  }

  interface AlertDetails {
    fun getTitle(): Int

    fun getDescription(): Int

    fun getPositiveText(): Int

    fun getNegativeText(): Int

    fun getPositiveClickListener()

    fun getNegativeClickListener()
  }
}