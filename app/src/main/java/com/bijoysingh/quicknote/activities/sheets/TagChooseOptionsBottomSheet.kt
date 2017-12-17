package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.items.TagOptionsItem
import com.github.bijoysingh.starter.prefs.DataStore

class TagChooseOptionsBottomSheet : TagOptionItemBottomSheetBase() {

  var note: Note? = null

  override fun setupViewWithDialog(dialog: Dialog) {
    if (note === null) {
      dismiss()
      return
    }

    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.tag_sheet_choose_tag)
  }

  override fun onNewTagClick() {
    val activity = context as ThemedActivity
    CreateOrEditTagBottomSheet.openSheet(activity, Tag.gen(), {tag, deleted ->
      note!!.toggleTag(tag)
      note!!.save(context)
      reset(dialog)
    })
  }

  private fun getOptions(): List<TagOptionsItem> {
    val options = ArrayList<TagOptionsItem>()
    val tags = note!!.tagIDs
    for (tag in Tag.db(context).all) {
      options.add(TagOptionsItem(
          tag = tag,
          listener = View.OnClickListener {
            note!!.toggleTag(tag)
            note!!.save(context)
            reset(dialog)
          },
          selected = tags.contains(tag.uid)
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = TagChooseOptionsBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.note = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}