package com.maubis.scarlet.base.main.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.support.recycler.RecyclerItem

class EmptyRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    itemView.setOnClickListener {
      val newNoteIntent = CreateNoteActivity.getNewNoteIntent(
        context,
        folder = (context as MainActivity).config.folders.firstOrNull()?.uuid ?: ""
      )
      context.startActivity(newNoteIntent)
    }
  }
}
