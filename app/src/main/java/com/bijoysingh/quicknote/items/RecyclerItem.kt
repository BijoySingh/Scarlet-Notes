package com.bijoysingh.quicknote.items

import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.recyclerview.*
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import java.util.*

abstract class RecyclerItem {

  abstract val type: Type

  enum class Type {
    NOTE,
    EMPTY,
    FILE,
    INFORMATION
  }

  companion object {
    fun getList(
        staggered: Boolean,
        isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
      val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.NOTE.ordinal)
          .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
          .holderClass(NoteRecyclerHolder::class.java)
          .build())
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.EMPTY.ordinal)
          .layoutFile(R.layout.item_no_notes)
          .holderClass(EmptyRecyclerHolder::class.java)
          .spanSize(2)
          .build())
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.INFORMATION.ordinal)
          .layoutFile(R.layout.item_information)
          .holderClass(InformationRecyclerHolder::class.java)
          .spanSize(2)
          .build())
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.FILE.ordinal)
          .layoutFile(R.layout.item_import_file)
          .holderClass(FileImportViewHolder::class.java)
          .build())
      return list
    }

    fun getSelectableList(
        staggered: Boolean,
        isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
      val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.NOTE.ordinal)
          .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
          .holderClass(SelectableNoteRecyclerViewHolder::class.java)
          .build())
      list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
          .viewType(Type.EMPTY.ordinal)
          .layoutFile(R.layout.item_no_notes)
          .holderClass(EmptyRecyclerHolder::class.java)
          .spanSize(2)
          .build())
      return list
    }
  }
}
