package com.maubis.scarlet.base.note.recycler

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppImageStorage
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.trim

open class NoteRecyclerViewHolderBase(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView
  protected val tags: TextView
  protected val image: ImageView
  protected val description: TextView
  protected val edit: ImageView
  protected val share: ImageView
  protected val delete: ImageView
  protected val copy: ImageView
  protected val moreOptions: ImageView
  protected val bottomLayout: View

  protected val pinIndicator: ImageView
  protected val unlockIndicator: ImageView
  protected val reminderIndicator: ImageView
  protected val stateIndicator: ImageView
  protected val backupIndicator: ImageView

  init {
    this.view = view as CardView
    tags = view.findViewById(R.id.tags)
    image = view.findViewById(R.id.image)
    description = view.findViewById(R.id.description)
    share = view.findViewById(R.id.share_button)
    delete = view.findViewById(R.id.delete_button)
    copy = view.findViewById(R.id.copy_button)
    moreOptions = view.findViewById(R.id.options_button)
    pinIndicator = view.findViewById(R.id.pin_icon)
    unlockIndicator = view.findViewById(R.id.unlock_icon)
    reminderIndicator = view.findViewById(R.id.reminder_icon)
    edit = view.findViewById(R.id.edit_button)
    bottomLayout = view.findViewById(R.id.bottom_toolbar_layout)
    stateIndicator = view.findViewById(R.id.state_icon)
    backupIndicator = view.findViewById(R.id.backup_icon)
  }

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as NoteRecyclerItem
    setDescription(item)
    setImage(item)
    setIndicators(item)
    setMetaText(item)

    view.alpha = if (item.note.isNoteLockedButAppUnlocked()) 0.7f else 1.0f
    view.setOnClickListener { viewClick(item.note, extra) }
    view.setOnLongClickListener {
      viewLongClick(item.note, extra)
      false
    }
    view.setCardBackgroundColor(item.backgroundColor)
    setActionBar(item, extra)
  }

  private fun setDescription(note: NoteRecyclerItem) {
    description.setTypeface(sAppTypeface.text(), Typeface.NORMAL)
    description.text = note.description
    description.maxLines = note.lineCount
    description.setTextColor(note.descriptionColor)
  }

  private fun setImage(note: NoteRecyclerItem) {
    val isImageAvailable = !note.imageSource.isBlank()
    image.visibility = visibility(isImageAvailable)
    if (isImageAvailable) {
      sAppImageStorage.loadThumbnailFileToImageView(note.note.uuid, note.imageSource, image)
    }
  }

  private fun setIndicators(note: NoteRecyclerItem) {
    pinIndicator.visibility = visibility(note.note.pinned)
    reminderIndicator.visibility = visibility(note.hasReminder)
    backupIndicator.visibility = visibility(note.disableBackup)
    when (note.state) {
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
    }
    unlockIndicator.visibility = visibility(note.note.locked)

    pinIndicator.setColorFilter(note.indicatorColor)
    stateIndicator.setColorFilter(note.indicatorColor)
    reminderIndicator.setColorFilter(note.indicatorColor)
    backupIndicator.setColorFilter(note.indicatorColor)
    unlockIndicator.setColorFilter(note.indicatorColor)
  }

  private fun setMetaText(note: NoteRecyclerItem) {
    tags.typeface = sAppTypeface.text()
    when {
      !TextUtils.isNullOrEmpty(note.tagsSource) -> {
        tags.setTextColor(note.tagsColor)
        val source = note.tags
        tags.text = trim(source)
        tags.visibility = VISIBLE
      }
      !TextUtils.isNullOrEmpty(note.timestamp) -> {
        tags.setTextColor(note.timestampColor)
        tags.text = note.timestamp
        tags.visibility = VISIBLE
      }
      else -> {
        tags.visibility = GONE
      }
    }
  }

  private fun setActionBar(note: NoteRecyclerItem, extra: Bundle?) {
    delete.setOnClickListener { deleteIconClick(note.note, extra) }
    share.setOnClickListener { shareIconClick(note.note, extra) }
    edit.setOnClickListener { editIconClick(note.note, extra) }
    copy.setOnClickListener { copyIconClick(note.note, extra) }
    moreOptions.setOnClickListener { moreOptionsIconClick(note.note, extra) }

    delete.setColorFilter(note.actionBarIconColor)
    share.setColorFilter(note.actionBarIconColor)
    edit.setColorFilter(note.actionBarIconColor)
    copy.setColorFilter(note.actionBarIconColor)
    moreOptions.setColorFilter(note.actionBarIconColor)
  }

  protected open fun viewClick(note: Note, extra: Bundle?) {}
  protected open fun viewLongClick(note: Note, extra: Bundle?) {}

  protected open fun deleteIconClick(note: Note, extra: Bundle?) {}
  protected open fun shareIconClick(note: Note, extra: Bundle?) {}
  protected open fun editIconClick(note: Note, extra: Bundle?) {}
  protected open fun copyIconClick(note: Note, extra: Bundle?) {}
  protected open fun moreOptionsIconClick(note: Note, extra: Bundle?) {}
}
