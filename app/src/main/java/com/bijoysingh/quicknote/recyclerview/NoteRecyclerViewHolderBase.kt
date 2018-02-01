package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.items.NoteRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.utils.NoteState
import com.bijoysingh.quicknote.utils.trim
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.TextUtils
import ru.noties.markwon.Markwon

open class NoteRecyclerViewHolderBase(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView
  protected val tags: TextView
  protected val title: TextView
  protected val description: TextView
  protected val edit: ImageView
  protected val share: ImageView
  protected val delete: ImageView
  protected val copy: ImageView
  protected val moreOptions: ImageView
  protected val bottomLayout: View

  protected val pinIndicator: ImageView
  protected val stateIndicator: ImageView

  init {
    this.view = view as CardView
    tags = view.findViewById(R.id.tags)
    title = view.findViewById(R.id.title)
    description = view.findViewById(R.id.description)
    share = view.findViewById(R.id.share_button)
    delete = view.findViewById(R.id.delete_button)
    copy = view.findViewById(R.id.copy_button)
    moreOptions = view.findViewById(R.id.options_button)
    pinIndicator = view.findViewById(R.id.pin_icon)
    edit = view.findViewById(R.id.edit_button)
    bottomLayout = view.findViewById(R.id.bottom_toolbar_layout)
    stateIndicator = view.findViewById(R.id.state_icon)
  }

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val isMarkdownEnabled = extra == null || extra.getBoolean(KEY_MARKDOWN_ENABLED, true)
    val lineCount = extra?.getInt(LineCountBottomSheet.KEY_LINE_COUNT, LineCountBottomSheet.LINE_COUNT_DEFAULT)
        ?: LineCountBottomSheet.LINE_COUNT_DEFAULT

    val item = itemData as NoteRecyclerItem
    val data = item.note
    val noteTitle = data.getTitle()
    title.text = noteTitle
    title.visibility = if (noteTitle.isEmpty()) View.GONE else View.VISIBLE

    description.text = data.getLockedText(context, isMarkdownEnabled)
    description.maxLines = lineCount

    pinIndicator.visibility = if (data.pinned) View.VISIBLE else View.GONE

    when (data.noteState) {
      NoteState.FAVOURITE -> {
        stateIndicator.visibility = View.VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_favorite_white_48dp)
      }
      NoteState.ARCHIVED -> {
        stateIndicator.visibility = View.VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_archive_white_48dp)
      }
      NoteState.TRASH -> {
        stateIndicator.visibility = View.VISIBLE
        stateIndicator.setImageResource(R.drawable.ic_delete_white_48dp)
      }
      NoteState.DEFAULT -> stateIndicator.visibility = GONE
      else -> stateIndicator.visibility = GONE
    }

    val noteTimestamp = data.displayTime
    if (!TextUtils.isNullOrEmpty(data.tags)) {
      tags.setTextColor(ContextCompat.getColor(context, R.color.light_secondary_text))
      val source = Markwon.markdown(context, data.getTagString(context))
      tags.text = trim(source)
      tags.visibility = VISIBLE
    } else if (!TextUtils.isNullOrEmpty(noteTimestamp)) {
      tags.setTextColor(ContextCompat.getColor(context, R.color.light_hint_text))
      tags.text = noteTimestamp
      tags.visibility = VISIBLE
    } else {
      tags.visibility = GONE
    }

    view.setOnClickListener { viewClick(data, extra) }
    view.setOnLongClickListener {
      viewLongClick(data, extra)
      false
    }
    view.setCardBackgroundColor(data.color)

    delete.setOnClickListener { deleteIconClick(data, extra) }
    share.setOnClickListener { shareIconClick(data, extra) }
    edit.setOnClickListener { editIconClick(data, extra) }
    copy.setOnClickListener { copyIconClick(data, extra) }
    moreOptions.setOnClickListener { moreOptionsIconClick(data, extra) }
  }

  protected open fun viewClick(note: Note, extra: Bundle?) {}
  protected open fun viewLongClick(note: Note, extra: Bundle?) {}

  protected open fun deleteIconClick(note: Note, extra: Bundle?) {}
  protected open fun shareIconClick(note: Note, extra: Bundle?) {}
  protected open fun editIconClick(note: Note, extra: Bundle?) {}
  protected open fun copyIconClick(note: Note, extra: Bundle?) {}
  protected open fun moreOptionsIconClick(note: Note, extra: Bundle?) {}
}
