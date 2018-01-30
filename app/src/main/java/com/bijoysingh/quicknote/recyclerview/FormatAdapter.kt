package com.bijoysingh.quicknote.recyclerview

import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.formats.Format
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import java.util.*


class FormatAdapter(internal var activity: ViewAdvancedNoteActivity)
  : MultiRecyclerViewAdapter<Format>(activity, Format.list), ItemTouchHelperAdapter {

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

