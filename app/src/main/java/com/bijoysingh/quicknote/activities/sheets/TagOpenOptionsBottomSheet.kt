package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.items.TagOptionsItem

class TagOpenOptionsBottomSheet : TagOptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.tag_sheet_choose_tag)
  }

  private fun getOptions(): List<TagOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<TagOptionsItem>()
    for (tag in Tag.db(context).all) {
      options.add(TagOptionsItem(
          tag = tag,
          listener = View.OnClickListener {
            activity.openTag(tag)
            dismiss()
          },
          editable = true,
          editListener = View.OnClickListener {
            CreateOrEditTagBottomSheet.openSheet(activity, tag, { _, _ -> reset(dialog) })
          }
      ))
    }
    return options
  }

  override fun onNewTagClick() {
    val activity = context as MainActivity
    CreateOrEditTagBottomSheet.openSheet(activity, Tag.gen(), { _, _ -> reset(dialog) })
  }

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = TagOpenOptionsBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}