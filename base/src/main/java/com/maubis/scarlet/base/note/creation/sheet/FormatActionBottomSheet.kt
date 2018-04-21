package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemedActivity
import pl.aprilapps.easyphotopicker.EasyImage

class FormatActionBottomSheet : GridBottomSheetBase() {

  var noteUUID: String = "default"
  var format: Format? = null

  override fun setupViewWithDialog(dialog: Dialog) {
    if (format === null) {
      return
    }

    setOptions(dialog, getOptions(noteUUID, format!!))
    setOptionTitle(dialog, R.string.format_action_title)
  }

  private fun getOptions(noteUUID: String, format: Format): List<OptionsItem> {
    val activity = themedActivity() as ViewAdvancedNoteActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.import_export_layout_exporting_share,
        subtitle = R.string.import_export_layout_exporting_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          IntentUtils.ShareBuilder(activity)
              .setChooserText(activity.getString(R.string.share_using))
              .setText(format.text)
              .share()
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.formatType)
    ))
    options.add(OptionsItem(
        title = R.string.format_action_copy,
        subtitle = R.string.format_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          TextUtils.copyToClipboard(context, format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.formatType)
    ))
    options.add(OptionsItem(
        title = R.string.format_action_camera,
        subtitle = R.string.format_action_camera,
        icon = R.drawable.ic_image_camera,
        listener = View.OnClickListener {
          EasyImage.openCamera(activity, format.uid)
        },
        visible = format.formatType === FormatType.IMAGE
    ))
    options.add(OptionsItem(
        title = R.string.format_action_gallery,
        subtitle = R.string.format_action_gallery,
        icon = R.drawable.ic_image_gallery,
        listener = View.OnClickListener {
          EasyImage.openGallery(activity, format.uid)
        },
        visible = format.formatType === FormatType.IMAGE
    ))
    options.add(OptionsItem(
        title = R.string.delete_sheet_delete_trash_yes,
        subtitle = R.string.delete_sheet_delete_trash_yes,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.deleteFormat(format)
          if (format.formatType === FormatType.IMAGE && !format.text.isBlank()) {
            val noteImage = NoteImage(themedContext())
            noteImage.deleteIfExist(noteImage.getFile(noteUUID, format))
          }
          dismiss()
        }
    ))
    return options
  }

  companion object {

    fun openSheet(activity: ThemedActivity, noteUUID: String, format: Format) {
      val sheet = FormatActionBottomSheet()
      sheet.format = format
      sheet.noteUUID = noteUUID
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}