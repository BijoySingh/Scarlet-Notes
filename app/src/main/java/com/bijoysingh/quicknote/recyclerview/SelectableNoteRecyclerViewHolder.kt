package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bijoysingh.quicknote.activities.INoteSelectorActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.items.RecyclerItem

class SelectableNoteRecyclerViewHolder(context: Context, view: View) : NoteRecyclerViewHolderBase(context, view) {

  private val noteSelector = context as INoteSelectorActivity

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    super.populate(itemData, extra)
    bottomLayout.visibility = View.GONE
  }

  override fun viewClick(note: Note, extra: Bundle?) {
    noteSelector.onNoteClicked(note)
  }
}
