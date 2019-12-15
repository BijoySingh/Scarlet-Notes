package com.maubis.scarlet.base.main.recycler

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.support.recycler.RecyclerItem

fun RecyclerViewHolder<RecyclerItem>.setFullSpan() {
  val layoutParams = itemView.layoutParams
  if (layoutParams is StaggeredGridLayoutManager.LayoutParams)
    layoutParams.isFullSpan = true
}