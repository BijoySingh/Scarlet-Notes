package com.maubis.scarlet.base.support.recycler

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SimpleItemTouchHelper(private val mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

  override fun isLongPressDragEnabled(): Boolean = true

  override fun isItemViewSwipeEnabled(): Boolean = true

  override fun getMovementFlags(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder): Int {
    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    val swipeFlags = 0 //ItemTouchHelper.START | ItemTouchHelper.END;
    return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
  }

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder): Boolean {
    mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    mAdapter.onItemDismiss(viewHolder.adapterPosition)
  }
}