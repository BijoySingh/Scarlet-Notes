package com.maubis.scarlet.base.note.recycler

import android.content.Context
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.export.recycler.FileImportViewHolder
import com.maubis.scarlet.base.main.recycler.EmptyRecyclerHolder
import com.maubis.scarlet.base.main.recycler.InformationRecyclerHolder
import com.maubis.scarlet.base.note.folder.FolderRecyclerHolder
import com.maubis.scarlet.base.note.folder.SelectorFolderRecyclerHolder
import com.maubis.scarlet.base.note.selection.recycler.SelectableNoteRecyclerViewHolder
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import java.util.*

class NoteAppAdapter : MultiRecyclerViewAdapter<RecyclerItem> {

  @JvmOverloads
  constructor(context: Context, staggered: Boolean = false, isTablet: Boolean = false) : super(
    context, getRecyclerItemControllerList(staggered, isTablet)) {
  }

  constructor(context: Context, list: List<MultiRecyclerViewControllerItem<RecyclerItem>>) : super(context, list) {}

  override fun getItemViewType(position: Int): Int {
    return items[position].type.ordinal
  }
}

fun getRecyclerItemControllerList(
  staggered: Boolean,
  isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.NOTE.ordinal)
      .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
      .holderClass(NoteRecyclerHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.EMPTY.ordinal)
      .layoutFile(R.layout.item_no_notes)
      .holderClass(EmptyRecyclerHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.INFORMATION.ordinal)
      .layoutFile(R.layout.item_information)
      .holderClass(InformationRecyclerHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.FILE.ordinal)
      .layoutFile(R.layout.item_import_file)
      .holderClass(FileImportViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.FOLDER.ordinal)
      .layoutFile(R.layout.item_folder)
      .holderClass(FolderRecyclerHolder::class.java)
      .build())
  return list
}

fun getSelectableRecyclerItemControllerList(
  staggered: Boolean,
  isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.NOTE.ordinal)
      .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
      .holderClass(SelectableNoteRecyclerViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.FOLDER.ordinal)
      .layoutFile(R.layout.item_selector_folder)
      .holderClass(SelectorFolderRecyclerHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.EMPTY.ordinal)
      .layoutFile(R.layout.item_no_notes)
      .holderClass(EmptyRecyclerHolder::class.java)
      .spanSize(2)
      .build())
  return list
}