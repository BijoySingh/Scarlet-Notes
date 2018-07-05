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

    val usageText = when {
      item.usage == 0 -> context.getString(R.string.folder_card_title)
      item.usage == 1 -> context.getString(R.string.folder_card_title_single_note)
      else ->context.getString(R.string.folder_card_title_notes, item.usage)
    }
    label.setText(usageText)

    timestamp.text = item.timestamp
    timestamp.setTextColor(item.timestampColor)

    view.setCardBackgroundColor(item.folder.color)
    view.setOnClickListener {
      item.click()
    }
    view.setOnLongClickListener {
      item.longClick()
      return@setOnLongClickListener false
    }
  }
}