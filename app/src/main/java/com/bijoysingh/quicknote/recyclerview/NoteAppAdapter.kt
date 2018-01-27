package com.bijoysingh.quicknote.recyclerview

import android.content.Context

import com.bijoysingh.quicknote.items.RecyclerItem
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem

class NoteAppAdapter : MultiRecyclerViewAdapter<RecyclerItem> {

  @JvmOverloads constructor(context: Context, staggered: Boolean = false, isTablet: Boolean = false) : super(context, RecyclerItem.getList(staggered, isTablet)) {}

  constructor(context: Context, list: List<MultiRecyclerViewControllerItem<RecyclerItem>>) : super(context, list) {}

  override fun getItemViewType(position: Int): Int {
    return items[position].type.ordinal
  }
}
