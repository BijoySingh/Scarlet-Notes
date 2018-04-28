package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.settings.view.ColorView
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


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
    setColorsList(colorPicker, getColorOptions())

    val colorPickerAccent = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout_accent)
    if (controller !== null) {
      setColorsList(colorPickerAccent, resources.getIntArray(R.array.bright_colors_accent))
    } else {
      colorPickerAccent.visibility = View.GONE
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.text = getSheetTitle()

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  private fun setColorsList(colorSelectorLayout: FlexboxLayout, colors: IntArray) {
    colorSelectorLayout.removeAllViews()
    val selectedColor: Int
    if (controller !== null) {
      selectedColor = controller!!.getNote().color
    } else if (defaultController !== null) {
      selectedColor = defaultController!!.getSelectedColor()
    } else {
      selectedColor = 0
    }

    for (color in colors) {
      val item = ColorView(context!!)

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

  private fun getColorOptions(): IntArray {
    val providedColorList = defaultController?.getColorList()
    return providedColorList ?: resources.getIntArray(R.array.bright_colors)
  }

  private fun getSheetTitle(): String {
    val resource = defaultController?.getSheetTitle() ?: R.string.choose_note_color
    return getString(resource)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_flexbox_layout

  interface ColorPickerController {
    fun onColorSelected(note: Note, color: Int)

    fun getNote(): Note
  }

  interface ColorPickerDefaultController {
    open fun getSheetTitle(): Int = R.string.choose_note_color

    open fun getColorList(): IntArray? = null

    fun onColorSelected(color: Int)

    fun getSelectedColor(): Int
  }

  companion object {
    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerController) {
      val sheet = ColorPickerBottomSheet()
      sheet.setPickerController(picker)

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerDefaultController) {
      val sheet = ColorPickerBottomSheet()
      sheet.setPickerController(picker)

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}