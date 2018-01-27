package com.bijoysingh.quicknote.recyclerview

interface ItemTouchHelperAdapter {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

  fun onItemDismiss(position: Int)
}