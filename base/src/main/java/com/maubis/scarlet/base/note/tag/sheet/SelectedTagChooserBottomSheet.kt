package com.maubis.scarlet.base.note.tag.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.note.getTagUUIDs
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.main.sheets.LithoTagOptionsItem
import com.maubis.scarlet.base.main.sheets.TagItemLayout
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle

class SelectedTagChooserBottomSheet : LithoBottomSheet() {

  var onActionListener: (Tag, Boolean) -> Unit = { _, _ -> }

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as SelectNotesActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
    val tagsComponent = Column.create(componentContext)
      .paddingDip(YogaEdge.TOP, 8f)
      .paddingDip(YogaEdge.BOTTOM, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.tag_sheet_choose_tag)
          .marginDip(YogaEdge.BOTTOM, 12f))
    getTagOptions().forEach {
      tagsComponent.child(TagItemLayout.create(componentContext).option(it))
    }

    val addTag = LithoOptionsItem(
      title = R.string.tag_sheet_new_tag_button,
      subtitle = 0,
      icon = R.drawable.icon_add_note,
      listener = {
        CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag()) { tag, _ ->
          onActionListener(tag, true)
          reset(activity, dialog)
        }
      })
    tagsComponent.child(OptionItemLayout.create(componentContext)
                          .option(addTag)
                          .backgroundRes(R.drawable.accent_rounded_bg)
                          .marginDip(YogaEdge.TOP, 16f)
                          .onClick { addTag.listener() })

    component.child(tagsComponent)
    return component.build()
  }

  private fun getTagOptions(): List<LithoTagOptionsItem> {
    val activity = context as SelectNotesActivity
    val options = ArrayList<LithoTagOptionsItem>()

    val tags = HashSet<String>()
    tags.addAll(activity.getAllSelectedNotes().firstOrNull()?.getTagUUIDs() ?: emptySet())

    activity.getAllSelectedNotes().forEach {
      val uuids = it.getTagUUIDs().toMutableSet()
      val uuidsToRemove = HashSet<String>()
      for (tag in tags) {
        if (!uuids.contains(tag)) {
          uuidsToRemove.add(tag)
        }
      }
      tags.removeAll(uuidsToRemove)
    }
    for (tag in CoreConfig.tagsDb.getAll()) {
      options.add(
        LithoTagOptionsItem(
          tag = tag,
          listener = {
            onActionListener(tag, !tags.contains(tag.uuid))
            activity.refreshSelectedNotes()
            reset(activity, dialog)
          },
          isSelected = tags.contains(tag.uuid)
        ))
    }
    options.sortByDescending { if (it.isSelected) 1 else 0 }
    return options
  }
}