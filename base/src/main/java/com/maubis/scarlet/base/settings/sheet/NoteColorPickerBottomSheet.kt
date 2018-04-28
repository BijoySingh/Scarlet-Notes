package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.settings.view.ColorView
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


class NoteColorPickerBottomSheet : ThemedBottomSheetFragment() {

  var colorChosen: Int = 0
  var defaultController: ColorPickerController? = null

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val controller = defaultController
    if (controller == null) {
      dismiss()
      return
    }

    val colorPicker = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout)
    setColorsList(controller, colorPicker, resources.getIntArray(R.array.bright_colors))

    val colorPickerAccent = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout_accent)
    setColorsList(controller, colorPickerAccent, resources.getIntArray(R.array.bright_colors_accent))

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setText(R.string.choose_note_color)
    optionsTitle.setOnClickListener {
      dismiss()
    }

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getBackgroundCardViewIds() = arrayOf(
      R.id.accent_color_card,
      R.id.core_color_card)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  private fun setColorsList(controller: ColorPickerController, colorSelectorLayout: FlexboxLayout, colors: IntArray) {
    colorSelectorLayout.removeAllViews()
    colorChosen = when {
      colorChosen != 0 -> colorChosen
      else -> controller.getNote().color
    }

    for (color in colors) {
      val item = ColorView(context!!)
      item.setColor(color, colorChosen == color)
      item.setOnClickListener {
        colorChosen = color
        controller.onColorSelected(controller.getNote(), colorChosen)
        setColorsList(controller, colorSelectorLayout, colors)
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
    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerController) {
      val sheet = NoteColorPickerBottomSheet()
      sheet.defaultController = picker
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}