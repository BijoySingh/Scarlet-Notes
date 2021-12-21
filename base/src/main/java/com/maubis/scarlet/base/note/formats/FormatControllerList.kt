package com.maubis.scarlet.base.note.formats

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.note.formats.recycler.FormatBulletViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatImageViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatListViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatQuoteViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatSeparatorViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatTextViewHolder
import com.maubis.scarlet.base.note.formats.recycler.NullFormatHolder
import java.util.*

fun getFormatControllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<Format>>()
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.TAG.ordinal)
      .layoutFile(R.layout.item_format_tag)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.TEXT.ordinal)
      .layoutFile(R.layout.item_format_text)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.HEADING.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.SUB_HEADING.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.HEADING_3.ordinal)
      .layoutFile(R.layout.item_format_heading)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.QUOTE.ordinal)
      .layoutFile(R.layout.item_format_quote)
      .holderClass(FormatQuoteViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CODE.ordinal)
      .layoutFile(R.layout.item_format_code)
      .holderClass(FormatTextViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_1.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_2.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.BULLET_3.ordinal)
      .layoutFile(R.layout.item_format_bullet)
      .holderClass(FormatBulletViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CHECKLIST_CHECKED.ordinal)
      .layoutFile(R.layout.item_format_list)
      .holderClass(FormatListViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal)
      .layoutFile(R.layout.item_format_list)
      .holderClass(FormatListViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.IMAGE.ordinal)
      .layoutFile(R.layout.item_format_image)
      .holderClass(FormatImageViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.SEPARATOR.ordinal)
      .layoutFile(R.layout.item_format_separator)
      .holderClass(FormatSeparatorViewHolder::class.java)
      .build())
  list.add(
    MultiRecyclerViewControllerItem.Builder<Format>()
      .viewType(FormatType.EMPTY.ordinal)
      .layoutFile(R.layout.item_format_fab_space)
      .holderClass(NullFormatHolder::class.java)
      .build())
  return list
}
