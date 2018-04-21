package com.maubis.scarlet.base.note.tag.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.getTagUUIDs
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.tag.TagOptionsItem
import com.maubis.scarlet.base.note.toggleTag
import com.maubis.scarlet.base.support.database.tagsDB
import com.maubis.scarlet.base.support.ui.ThemedActivity

class TagChooseOptionsBottomSheet : TagOptionItemBottomSheetBase() {

  var note: Note? = null
  var dismissListener: () -> Unit = {}

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
    CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag(), { tag, _ ->
      note!!.toggleTag(tag)
      note!!.save(activity)
      reset(dialog)
    })
  }

  override fun onDismiss(dialog: DialogInterface?) {
    super.onDismiss(dialog)
    dismissListener()
  }

  private fun getOptions(): List<TagOptionsItem> {
    val options = ArrayList<TagOptionsItem>()
    val tags = note!!.getTagUUIDs()
    for (tag in tagsDB.getAll()) {
      options.add(TagOptionsItem(
          tag = tag,
          listener = View.OnClickListener {
            note!!.toggleTag(tag)
            note!!.save(themedContext())
            reset(dialog)
          },
          selected = tags.contains(tag.uuid)
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note, dismissListener: () -> Unit) {
      val sheet = TagChooseOptionsBottomSheet()

      sheet.note = note
      sheet.dismissListener = dismissListener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}