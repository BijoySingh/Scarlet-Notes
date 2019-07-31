package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.util.ToastHelper
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.note.ImageLoadCallback
import com.maubis.scarlet.base.main.sheets.openDeleteFormatDialog
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.maybeThrow
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class FormatImageViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  protected val text: TextView = root.findViewById(R.id.text)
  protected val image: ImageView = root.findViewById(R.id.image)

  protected val actionCamera: ImageView = root.findViewById(R.id.action_camera)
  protected val actionGallery: ImageView = root.findViewById(R.id.action_gallery)
  protected val actionMove: ImageView = root.findViewById(R.id.action_move_icon)
  protected val imageToolbar: View = root.findViewById(R.id.image_toolbar)
  protected val noImageMessage: UITextView = root.findViewById(R.id.no_image_message)

  protected var format: Format? = null

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    format = data

    text.setTextColor(config.secondaryTextColor)
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    text.setOnClickListener {
      EasyImage.openGallery(context as AppCompatActivity, data.uid)
    }

    noImageMessage.visibility = View.GONE
    noImageMessage.setTextColor(config.tertiaryTextColor)
    noImageMessage.setOnClickListener {
      openDeleteFormatDialog(activity, data)
    }

    val iconColor = config.iconColor
    noImageMessage.setImageTint(iconColor)
    actionCamera.setColorFilter(iconColor)
    actionGallery.setColorFilter(iconColor)
    actionCamera.setOnClickListener {
      try {
        EasyImage.openCamera(context as AppCompatActivity, data.uid)
      } catch (exception: Exception) {
        ToastHelper.show(context, "No camera app installed")
        maybeThrow(context as AppCompatActivity, exception)
      }
    }
    actionGallery.setOnClickListener {
      try {
        EasyImage.openGallery(context as AppCompatActivity, data.uid)
      } catch (exception: Exception) {
        ToastHelper.show(context, "No photo picker app installed")
        maybeThrow(context as AppCompatActivity, exception)
      }
    }
    actionMove.setColorFilter(config.iconColor)
    actionMove.setOnClickListener {
      openSheet(activity, FormatActionBottomSheet().apply {
        noteUUID = config.noteUUID
        format = data
      })
    }
    imageToolbar.visibility = visibility(config.editable)

    val imageToolbarBg = config.backgroundColor
    imageToolbar.setBackgroundColor(imageToolbarBg)
    noImageMessage.setBackgroundColor(imageToolbarBg)

    val fileName = data.text
    if (!fileName.isBlank()) {
      val file = noteImagesFolder.getFile(config.noteUUID, data)
      when (file.exists()) {
        true -> populateFile(file)
        false -> {
          noImageMessage.setText(R.string.image_not_on_current_device)
          noImageMessage.visibility = visibility(config.editable)
          image.visibility = View.GONE
          imageToolbar.visibility = View.GONE
        }
      }
    }
  }

  fun populateFile(file: File) {
    noteImagesFolder.loadPersistentFileToImageView(image, file, object : ImageLoadCallback {
      override fun onSuccess() {
        noImageMessage.visibility = View.GONE
      }

      override fun onError() {
        noImageMessage.visibility = View.VISIBLE
        noImageMessage.setText(R.string.image_cannot_be_loaded)
      }
    })
  }
}
