package com.maubis.scarlet.base.note.formats

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.support.recycler.ItemTouchHelperAdapter
import java.util.*

class FormatAdapter(val formatActivity: IFormatRecyclerViewActivity)
  : MultiRecyclerViewAdapter<Format>(formatActivity.context(), formatActivity.controllerItems()), ItemTouchHelperAdapter {

  override fun getItemViewType(position: Int): Int {
    return items[position].formatType.ordinal
  }

  override fun onItemDismiss(position: Int) {
    formatActivity.deleteFormat(items[position])
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
    formatActivity.moveFormat(fromPosition, toPosition)
    return true
  }
}