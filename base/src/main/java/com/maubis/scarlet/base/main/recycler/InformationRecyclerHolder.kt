package com.maubis.scarlet.base.main.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.recycler.RecyclerItem

class InformationRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  val text: TextView = findViewById(R.id.information_text)
  val title: UITextView = findViewById(R.id.ui_information_title)

  override fun populate(data: RecyclerItem, extra: Bundle) {
    if (data !is InformationRecyclerItem) {
      return
    }
    title.setText(data.title)
    title.setImageResource(data.icon)
    text.setText(data.source)
    itemView.setOnClickListener {
      data.function()
    }
  }
}
