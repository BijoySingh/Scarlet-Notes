package com.bijoysingh.quicknote.formats

import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.formats.recycler.FormatImageViewHolder
import com.bijoysingh.quicknote.formats.recycler.FormatListViewHolder
import com.bijoysingh.quicknote.formats.recycler.FormatSeparatorViewHolder
import com.bijoysingh.quicknote.formats.recycler.FormatTextViewHolder
import com.bijoysingh.quicknote.recyclerview.EmptyFormatHolder
import com.bijoysingh.quicknote.recyclerview.ItemTouchHelperAdapter
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.format.Format
import com.maubis.scarlet.base.format.FormatType
import java.util.*

class FormatAdapter(internal var activity: ViewAdvancedNoteActivity)
  : MultiRecyclerViewAdapter<Format>(activity, getFormatControllerItems()), ItemTouchHelperAdapter {

  override fun getItemViewType(position: Int): Int {
    return items[position].formatType.ordinal
  }

  override fun onItemDismiss(position: Int) {
    activity.deleteFormat(items[position])
  }

  override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(items, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(items, i, i - 1)
      }
    }
    notifyItemMoved(fromPosition, toPosition)
    activity.moveFormat(fromPosition, toPosition)
    return true
  }
}

fun getFormatControllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<Format>>()
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.TAG.ordinal)
          .layoutFile(R.layout.item_format_tag)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.TEXT.ordinal)
          .layoutFile(R.layout.item_format_text)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.HEADING.ordinal)
          .layoutFile(R.layout.item_format_heading)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.SUB_HEADING.ordinal)
          .layoutFile(R.layout.item_format_sub_heading)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.QUOTE.ordinal)
          .layoutFile(R.layout.item_format_quote)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CODE.ordinal)
          .layoutFile(R.layout.item_format_code)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CHECKLIST_CHECKED.ordinal)
          .layoutFile(R.layout.item_format_list)
          .holderClass(FormatListViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal)
          .layoutFile(R.layout.item_format_list)
          .holderClass(FormatListViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.IMAGE.ordinal)
          .layoutFile(R.layout.item_format_image)
          .holderClass(FormatImageViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.SEPARATOR.ordinal)
          .layoutFile(R.layout.item_format_separator)
          .holderClass(FormatSeparatorViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.EMPTY.ordinal)
          .layoutFile(R.layout.item_format_fab_space)
          .holderClass(EmptyFormatHolder::class.java)
          .build())
  return list
}
