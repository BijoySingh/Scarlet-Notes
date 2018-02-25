package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.INTENT_KEY_NOTE_ID
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.sheets.AlertBottomSheet
import com.bijoysingh.quicknote.activities.sheets.FormatActionBottomSheet
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.KEY_TEXT_SIZE
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.TEXT_SIZE_DEFAULT
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder.Companion.KEY_EDITABLE
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.uibasics.views.UITextView
import com.squareup.picasso.Callback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class FormatImageViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  protected val activity: ViewAdvancedNoteActivity
  protected val text: TextView
  protected val image: ImageView

  protected val actionCamera: ImageView
  protected val actionGallery: ImageView
  protected val actionMove: View
  protected val imageToolbar: View
  protected val noImageMessage: UITextView

  protected var format: Format? = null

  init {
    text = view.findViewById<View>(R.id.text) as TextView
    image = view.findViewById<ImageView>(R.id.image) as ImageView
    actionCamera = view.findViewById<ImageView>(R.id.action_camera) as ImageView
    actionGallery = view.findViewById<ImageView>(R.id.action_gallery) as ImageView
    activity = context as ViewAdvancedNoteActivity
    actionMove = view.findViewById(R.id.action_move)
    imageToolbar = view.findViewById(R.id.image_toolbar)
    noImageMessage = view.findViewById<UITextView>(R.id.no_image_message)
  }

  override fun populate(data: Format, extra: Bundle?) {
    format = data
    val editable = !(extra != null
        && extra.containsKey(KEY_EDITABLE)
        && !extra.getBoolean(KEY_EDITABLE))
    val fontSize = extra?.getInt(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
        ?: TextSizeBottomSheet.TEXT_SIZE_DEFAULT
    val noteUUID: String = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default"

    val theme = ThemeManager.get(context)
    text.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    text.setOnClickListener {
      EasyImage.openGallery(context as AppCompatActivity, data.uid)
    }

    noImageMessage.visibility = View.GONE
    noImageMessage.setTextColor(theme.get(context, ThemeColorType.TERTIARY_TEXT))
    noImageMessage.setOnClickListener {
      if (getAppFlavor() != Flavor.NONE) {
        AlertBottomSheet.openDeleteFormatDialog(activity, data)
      }
    }

    val iconColor = theme.get(context, ThemeColorType.TOOLBAR_ICON)
    noImageMessage.setImageTint(iconColor)
    actionCamera.setColorFilter(iconColor)
    actionGallery.setColorFilter(iconColor)
    actionCamera.setOnClickListener {
      EasyImage.openCamera(context as AppCompatActivity, data.uid)
    }
    actionGallery.setOnClickListener {
      EasyImage.openGallery(context as AppCompatActivity, data.uid)
    }
    actionMove.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, noteUUID, data)
    }
    imageToolbar.visibility = visibility(editable)

    val imageToolbarBg = theme.getThemedColor(context, R.color.material_grey_200, R.color.material_grey_700)
    imageToolbar.setBackgroundColor(imageToolbarBg)
    noImageMessage.setBackgroundColor(imageToolbarBg)

    val fileName = data.text
    if (!fileName.isBlank()) {
      val file = getFile(context, noteUUID, data)
      when (file.exists()) {
        true -> populateFile(file)
        false -> {
          noImageMessage.setText(R.string.image_not_on_current_device)
          noImageMessage.visibility = visibility(editable)
          image.visibility = View.GONE
          imageToolbar.visibility = View.GONE
        }
      }
    }
  }

  fun populateFile(file: File) {
    loadFileToImageView(context, image, file, object : Callback {
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
