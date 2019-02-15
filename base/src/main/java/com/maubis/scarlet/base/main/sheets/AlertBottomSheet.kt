package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity

data class AlertSheetConfig(
    val title: Int = R.string.delete_sheet_are_you_sure,
    val description: Int = R.string.delete_sheet_delete_note_permanently,
    val positiveText: Int = R.string.delete_sheet_delete_trash_yes,
    val negativeText: Int = R.string.delete_sheet_delete_trash_no,
    val onPositiveClick: () -> Unit = {},
    val onNegativeClick: () -> Unit = {})

class AlertBottomSheet : LithoBottomSheet() {
  var config: AlertSheetConfig = AlertSheetConfig()

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as ThemedActivity
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(config.title)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .textRes(config.description)
            .marginDip(YogaEdge.BOTTOM, 16f)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(config.positiveText)
            .onPrimaryClick {
              config.onPositiveClick()
              dismiss()
            }.secondaryActionRes(config.negativeText)
            .onSecondaryClick {
              config.onNegativeClick()
              dismiss()
            }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}

fun openDeleteNotePermanentlySheet(activity: ThemedActivity, note: Note, onDelete: () -> Unit) {
  openSheet(activity, AlertBottomSheet().apply {
    this.config = AlertSheetConfig(
        title = R.string.delete_sheet_are_you_sure,
        description = R.string.delete_sheet_delete_note_permanently,
        positiveText = R.string.delete_sheet_delete_trash_yes,
        negativeText = R.string.delete_sheet_delete_trash_no,
        onPositiveClick = {
          note.delete(activity)
          onDelete()
        },
        onNegativeClick = {})
  })
}

fun openDeleteAllXSheet(activity: MainActivity, subtitle: Int, onSuccess: () -> Unit) {
  openSheet(activity, AlertBottomSheet().apply {
    this.config = AlertSheetConfig(
        title = R.string.delete_sheet_are_you_sure,
        description = subtitle,
        positiveText = R.string.delete_sheet_delete_trash_yes,
        negativeText = R.string.delete_sheet_delete_trash_no,
        onPositiveClick = {
          onSuccess()
        },
        onNegativeClick = {})
  })
}

fun openDeleteFormatDialog(activity: ViewAdvancedNoteActivity, format: Format) {
  openSheet(activity, AlertBottomSheet().apply {
    this.config = AlertSheetConfig(
        title = R.string.delete_sheet_are_you_sure,
        description = R.string.image_delete_all_devices,
        positiveText = R.string.delete_sheet_delete_trash_yes,
        negativeText = R.string.delete_sheet_delete_trash_no,
        onPositiveClick = {
          activity.deleteFormat(format)
        },
        onNegativeClick = {})
  })
}

const val STORE_KEY_IMAGE_SYNC_NOTICE = "IMAGE_SYNC_NOTICE"
var sImageSyncNoticeShown: Int
  get() = CoreConfig.instance.store().get(STORE_KEY_IMAGE_SYNC_NOTICE, 0)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_IMAGE_SYNC_NOTICE, value)


fun openImageNotSynced(activity: ThemedActivity) {
  openSheet(activity, AlertBottomSheet().apply {
    this.config = AlertSheetConfig(
        title = R.string.image_not_uploaded,
        description = R.string.image_not_uploaded_details,
        positiveText = R.string.image_not_uploaded_i_understand,
        negativeText = R.string.delete_sheet_delete_trash_no,
        onPositiveClick = { sImageSyncNoticeShown = 1 },
        onNegativeClick = {})
  })
}

fun openDeleteTrashSheet(activity: MainActivity) {
  openSheet(activity, AlertBottomSheet().apply {
    this.config = AlertSheetConfig(
        title = R.string.delete_sheet_are_you_sure,
        description = R.string.delete_sheet_delete_trash,
        positiveText = R.string.delete_sheet_delete_trash_yes,
        negativeText = R.string.delete_sheet_delete_trash_no,
        onPositiveClick = {
          val notes = CoreConfig.notesDb.getByNoteState(arrayOf(NoteState.TRASH.name))
          for (note in notes) {
            note.delete(activity)
          }
          activity.setupData()
        },
        onNegativeClick = {})
  })
}