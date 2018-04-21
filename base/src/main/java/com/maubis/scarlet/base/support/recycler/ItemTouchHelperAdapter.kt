package com.maubis.scarlet.base.support.recycler

interface ItemTouchHelperAdapter {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

  fun onItemDismiss(position: Int)
}