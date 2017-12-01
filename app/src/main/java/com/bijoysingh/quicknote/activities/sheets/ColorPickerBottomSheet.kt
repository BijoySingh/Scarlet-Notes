package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.views.ColorView
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.google.android.flexbox.FlexboxLayout


class ColorPickerBottomSheet : SimpleBottomSheetFragment() {

  var controller: ColorPickerController? = null

  fun setPickerController(pickerController: ColorPickerController) {
    controller = pickerController
  }

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    if (controller == null) {
      dismiss()
      return
    }

    val colorPicker = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout)
    setColorsList(controller!!, colorPicker)
  }

  private fun setColorsList(
      controller: ColorPickerController,
      colorSelectorLayout: FlexboxLayout) {
    colorSelectorLayout.removeAllViews()
    val colors = resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(context)
      item.setColor(color, controller.getNote().color == color)
      item.root.setOnClickListener {
        controller.onColorSelected(controller.getNote(), color)
        dismiss()
      }
      colorSelectorLayout.addView(item)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_flexbox_layout

  interface ColorPickerController {
    fun onColorSelected(note: Note, color: Int)

    fun getNote(): Note
  }

  companion object {
    fun openSheet(activity: AppCompatActivity, picker: ColorPickerController) {
      val sheet = ColorPickerBottomSheet()
      sheet.setPickerController(picker)
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}