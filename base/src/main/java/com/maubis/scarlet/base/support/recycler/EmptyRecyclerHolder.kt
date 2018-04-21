package com.maubis.scarlet.base.support.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

class NullRecyclerItemHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  override fun populate(data: RecyclerItem, extra: Bundle) {

  }
}
