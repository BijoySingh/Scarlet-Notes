package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.recycler.RecyclerItem

class FolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView
  protected val label: UITextView
  protected val title: TextView
  protected val timestamp: TextView

  init {
    this.view = view as CardView
    title = view.findViewById(R.id.title)
    timestamp = view.findViewById(R.id.timestamp)
    label = view.findViewById(R.id.ui_information_title)
  }

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as FolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)

    timestamp.text = item.timestamp
    timestamp.setTextColor(item.timestampColor)

    view.setCardBackgroundColor(item.folder.color)
    view.setOnClickListener {

    }
    view.setOnLongClickListener {
      return@setOnLongClickListener false
    }
  }
}