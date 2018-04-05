package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.InformationRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

class InformationRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  val text: TextView

  init {
    text = findViewById(R.id.information_text)
  }

  override fun populate(data: RecyclerItem, extra: Bundle) {
    if (data !is InformationRecyclerItem) {
      return
    }
    text.setText(data.source)
    itemView.setOnClickListener {
      data.function()
    }
  }
}
