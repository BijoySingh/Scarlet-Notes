package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.support.sheets.GridOptionBottomSheet
import com.maubis.scarlet.base.support.specs.GridSectionItem
import com.maubis.scarlet.base.support.specs.GridSectionOptionItem
import pl.aprilapps.easyphotopicker.EasyImage

class FormatActionBottomSheet : GridOptionBottomSheet() {

  var noteUUID: String = "default"
  var format: Format? = null

  override fun title(): Int = R.string.format_action_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem> {
    val activity = componentContext.androidContext as ViewAdvancedNoteActivity

    val sections = ArrayList<GridSectionItem>()
    val options = ArrayList<GridSectionOptionItem>()

    if (this.format === null) {
      return sections
    }

    val format: Format = this.format!!
    options.add(GridSectionOptionItem(
        label = R.string.import_export_layout_exporting_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = {
          IntentUtils.ShareBuilder(activity)
              .setChooserText(activity.getString(R.string.share_using))
              .setText(format.text)
              .share()
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.formatType)
    ))
    options.add(GridSectionOptionItem(
        label = R.string.format_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = {
          TextUtils.copyToClipboard(context, format.text)
          dismiss()
        },
        visible = !arrayOf(FormatType.IMAGE, FormatType.SEPARATOR).contains(format.formatType)
    ))
    options.add(GridSectionOptionItem(
        label = R.string.format_action_camera,
        icon = R.drawable.ic_image_camera,
        listener = {
          EasyImage.openCamera(activity, format.uid)
        },
        visible = format.formatType === FormatType.IMAGE
    ))
    options.add(GridSectionOptionItem(
        label = R.string.format_action_gallery,
        icon = R.drawable.ic_image_gallery,
        listener = {
          EasyImage.openGallery(activity, format.uid)
        },
        visible = format.formatType === FormatType.IMAGE
    ))
    options.add(GridSectionOptionItem(
        label = R.string.delete_sheet_delete_trash_yes,
        icon = R.drawable.ic_delete_white_48dp,
        listener = {
          activity.deleteFormat(format)
          if (format.formatType === FormatType.IMAGE && !format.text.isBlank()) {
            deleteIfExist(noteImagesFolder.getFile(noteUUID, format))
          }
          dismiss()
        }
    ))

    sections.add(GridSectionItem(options = options))
    return sections
  }
}