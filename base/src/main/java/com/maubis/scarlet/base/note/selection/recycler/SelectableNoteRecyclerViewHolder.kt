package com.maubis.scarlet.base.note.selection.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.recycler.NoteRecyclerViewHolderBase
import com.maubis.scarlet.base.note.selection.activity.INoteSelectorActivity
import com.maubis.scarlet.base.support.recycler.RecyclerItem

class SelectableNoteRecyclerViewHolder(context: Context, view: View) : NoteRecyclerViewHolderBase(context, view) {

  private val noteSelector = context as INoteSelectorActivity

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    super.populate(itemData, extra)
    bottomLayout.visibility = View.GONE

    val note = (itemData as NoteRecyclerItem).note
    itemView.alpha = if (noteSelector.isNoteSelected(note)) 1.0f else 0.5f
  }

  override fun viewClick(note: Note, extra: Bundle?) {
    noteSelector.onNoteClicked(note)
  }
}
