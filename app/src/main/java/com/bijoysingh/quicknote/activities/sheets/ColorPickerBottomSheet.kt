package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.views.ColorView
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.google.android.flexbox.FlexboxLayout


class ColorPickerBottomSheet : ThemedBottomSheetFragment() {

  var controller: ColorPickerController? = null
  var defaultController: ColorPickerDefaultController? = null

  fun setPickerController(pickerController: ColorPickerController) {
    controller = pickerController
  }

  fun setPickerController(pickerController: ColorPickerDefaultController) {
    defaultController = pickerController
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    if (controller == null && defaultController == null) {
      dismiss()
      return
    }

    val colorPicker = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout)
    setColorsList(colorPicker)

    maybeSetTextNightModeColor(dialog, R.id.options_title, R.color.light_tertiary_text)
  }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  private fun setColorsList(colorSelectorLayout: FlexboxLayout) {
    colorSelectorLayout.removeAllViews()
    val selectedColor: Int
    if (controller !== null) {
      selectedColor = controller!!.getNote().color
    } else if (defaultController !== null) {
      selectedColor = defaultController!!.getSelectedColor()
    } else {
      selectedColor = 0
    }

    val colors = resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(context)

      item.setColor(color, selectedColor == color)
      item.root.setOnClickListener {
        if (controller !== null) {
          controller!!.onColorSelected(controller!!.getNote(), color)
        } else if (defaultController !== null) {
          defaultController!!.onColorSelected(color)
        }
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

  interface ColorPickerDefaultController {
    fun onColorSelected(color: Int)

    fun getSelectedColor(): Int
  }

  companion object {
    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerController) {
      val sheet = ColorPickerBottomSheet()
      sheet.setPickerController(picker)
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerDefaultController) {
      val sheet = ColorPickerBottomSheet()
      sheet.setPickerController(picker)
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}