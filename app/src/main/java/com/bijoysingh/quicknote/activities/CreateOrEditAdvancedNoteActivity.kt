package com.bijoysingh.quicknote.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.ColorPickerBottomSheet
import com.bijoysingh.quicknote.activities.sheets.NoteFormatOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.NoteMarkdownOptionsBottomSheet
import com.bijoysingh.quicknote.database.utils.*
import com.bijoysingh.quicknote.formats.recycler.FormatImageViewHolder
import com.bijoysingh.quicknote.formats.recycler.FormatTextViewHolder
import com.bijoysingh.quicknote.recyclerview.SimpleItemTouchHelper
import com.bijoysingh.quicknote.utils.CircleDrawable
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.bind
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.format.*
import com.maubis.scarlet.base.note.NoteBuilder
import com.maubis.scarlet.base.note.NoteImage
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*


open class CreateOrEditAdvancedNoteActivity : ViewAdvancedNoteActivity() {

  private var active = false
  private var lastNoteInstance: Note? = null
  private var maxUid = 0
  private var toolbarMode: ToolbarMode = ToolbarMode.FORMAT

  val text: ImageView by bind(R.id.format_text)
  val heading: ImageView by bind(R.id.format_heading)
  val subHeading: ImageView by bind(R.id.format_sub_heading)
  val checkList: ImageView by bind(R.id.format_check_list)
  val formatMore: ImageView by bind(R.id.format_more)

  val markdownBold: ImageView by bind(R.id.markdown_bold)
  val markdownHeading: ImageView by bind(R.id.markdown_heading)
  val markdownItalics: ImageView by bind(R.id.markdown_italics)
  val markdownUnordered: ImageView by bind(R.id.markdown_unordered)
  val markdownMore: ImageView by bind(R.id.markdown_more)

  val chevronLeft: ImageView by bind(R.id.toolbar_chevron_left)
  val chevronRight: ImageView by bind(R.id.toolbar_chevron_right)

  override val editModeValue: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTouchListener()
    startHandler()
    lastNoteInstance = NoteBuilder().copy(note!!)
  }

  override fun setEditMode() {
    setEditMode(editModeValue)
  }

  private fun setTouchListener() {
    val callback = SimpleItemTouchHelper(adapter)
    val touchHelper = ItemTouchHelper(callback)
    touchHelper.attachToRecyclerView(formatsView)
  }

  override fun setNote() {
    super.setNote()
    maxUid = formats.size + 1
    val isEmpty = formats.isEmpty()
    if (isEmpty || (formats[0].formatType !== FormatType.HEADING
            && formats[0].formatType !== FormatType.IMAGE)) {
      addEmptyItem(0, FormatType.HEADING)
    }
    if (isEmpty) {
      addDefaultItem()
    }
  }

  protected open fun addDefaultItem() {
    addEmptyItem(FormatType.TEXT)
  }

  override fun setBottomToolbar() {
    setFormatToolbar()
    setMarkdownButtonToolbar()
    chevronLeft.setOnClickListener { toggleToolbarMode() }
    chevronRight.setOnClickListener { toggleToolbarMode() }
  }

  fun setFormatToolbar() {
    text.setOnClickListener { addEmptyItemAtFocused(FormatType.TEXT) }
    heading.setOnClickListener { addEmptyItemAtFocused(FormatType.HEADING) }
    subHeading.setOnClickListener { addEmptyItemAtFocused(FormatType.SUB_HEADING) }
    checkList.setOnClickListener { addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) }
    formatMore.setOnClickListener {
      NoteFormatOptionsBottomSheet.openSheet(this)
    }
  }

  fun setMarkdownButtonToolbar() {
    markdownBold.setOnClickListener { triggerMarkdown(MarkdownType.BOLD) }
    markdownItalics.setOnClickListener { triggerMarkdown(MarkdownType.ITALICS) }
    markdownHeading.setOnClickListener { triggerMarkdown(MarkdownType.HEADER) }
    markdownUnordered.setOnClickListener { triggerMarkdown(MarkdownType.UNORDERED) }
    markdownMore.setOnClickListener {
      NoteMarkdownOptionsBottomSheet.openSheet(this)
    }
  }

  fun toggleToolbarMode() {
    toolbarMode = when (toolbarMode) {
      ToolbarMode.FORMAT -> ToolbarMode.MARKDOWN
      ToolbarMode.MARKDOWN -> ToolbarMode.FORMAT
    }
    notifyToolbarModeChange()
  }

  fun notifyToolbarModeChange() {
    when (toolbarMode) {
      ToolbarMode.FORMAT -> {
        formatToolbar.visibility = VISIBLE
        markdownToolbar.visibility = GONE
      }
      ToolbarMode.MARKDOWN -> {
        formatToolbar.visibility = GONE
        markdownToolbar.visibility = VISIBLE
      }
    }
  }

  override fun setTopToolbar() {
    actionDelete.visibility = GONE
    actionShare.visibility = GONE
    actionCopy.visibility = GONE

    val colorButtonClicker = findViewById<View>(R.id.color_button_clicker)
    colorButtonClicker.setOnClickListener {
      ColorPickerBottomSheet.openSheet(
          this@CreateOrEditAdvancedNoteActivity,
          object : ColorPickerBottomSheet.ColorPickerController {
            override fun onColorSelected(note: Note, color: Int) = setNoteColor(color)
            override fun getNote(): Note = note!!
          })
    }
  }

  override fun notifyToolbarColor() {
    super.notifyToolbarColor()
    val theme = appTheme()
    toolbar.setBackgroundColor(theme.get(ThemeColorType.TOOLBAR_BACKGROUND))
    markdownToolbar.setBackgroundColor(theme.get(ThemeColorType.TOOLBAR_BACKGROUND))

    val toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    text.setColorFilter(toolbarIconColor)
    heading.setColorFilter(toolbarIconColor)
    subHeading.setColorFilter(toolbarIconColor)
    checkList.setColorFilter(toolbarIconColor)
    formatMore.setColorFilter(toolbarIconColor)

    markdownHeading.setColorFilter(toolbarIconColor)
    markdownBold.setColorFilter(toolbarIconColor)
    markdownUnordered.setColorFilter(toolbarIconColor)
    markdownItalics.setColorFilter(toolbarIconColor)
    markdownMore.setColorFilter(toolbarIconColor)

    val hintColor = theme.get(ThemeColorType.PRIMARY_TEXT)
    chevronLeft.setColorFilter(hintColor)
    chevronRight.setColorFilter(hintColor)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
      override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
        if (imageFile == null) {
          return
        }

        val targetFile = NoteImage(context).renameOrCopy(note!!, imageFile)
        val index = getFormatIndex(type)
        triggerImageLoaded(index, targetFile)
      }

      override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {
        //Some error handling
      }
    })
  }

  override fun onPause() {
    super.onPause()
    active = false
    maybeUpdateNoteWithoutSync()
    val destroyed = destroyIfNeeded()
    if (!destroyed) {
      note!!.saveToSync()
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    tryClosingTheKeyboard()
  }

  override fun onResume() {
    super.onResume()
    active = true
  }

  override fun onResumeAction() {
    // do nothing
  }

  private fun destroyIfNeeded(): Boolean {
    if (note!!.isUnsaved()) {
      return true
    }
    if (note!!.getFormats().isEmpty()) {
      note!!.delete(this)
      return true
    }
    return false
  }

  protected fun maybeUpdateNoteWithoutSync() {
    val vNote = note!!
    val vLastNoteInstance = lastNoteInstance ?: note!!

    vNote.description = FormatBuilder().getDescription(formats)

    // Ignore update if nothing changed. It allows for one undo per few seconds
    if (vNote.isEqual(vLastNoteInstance)) {
      return
    }

    vNote.updateTimestamp = Calendar.getInstance().timeInMillis
    maybeSaveNote(false)
    vLastNoteInstance.copyNote(vNote)
  }

  private fun startHandler() {
    val handler = Handler()
    handler.postDelayed(object : Runnable {
      override fun run() {
        if (active) {
          maybeUpdateNoteWithoutSync()
          handler.postDelayed(this, ViewAdvancedNoteActivity.Companion.HANDLER_UPDATE_TIME.toLong())
        }
      }
    }, ViewAdvancedNoteActivity.Companion.HANDLER_UPDATE_TIME.toLong())
  }

  protected fun addEmptyItem(type: FormatType) {
    addEmptyItem(formats.size, type)
  }

  private fun addEmptyItem(position: Int, type: FormatType) {
    val format = Format(type)
    format.uid = maxUid + 1
    maxUid++

    formats.add(position, format)
    adapter.addItem(format, position)
  }

  fun addEmptyItemAtFocused(type: FormatType) {
    if (focusedFormat == null) {
      addEmptyItem(type)
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      addEmptyItem(type)
      return
    }

    val newPosition = position + 1
    addEmptyItem(newPosition, type)
    formatsView.layoutManager.scrollToPosition(newPosition)
    focus(newPosition)
  }

  fun focus(position: Int) {
    val handler = Handler()
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestEditTextFocus()
    }, 100)
  }

  fun triggerMarkdown(markdownType: MarkdownType) {
    if (focusedFormat == null) {
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      return
    }

    val handler = Handler()
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestMarkdownAction(markdownType)
    }, 100)
  }

  fun triggerImageLoaded(position: Int, file: File) {
    if (position == -1) {
      return
    }

    val holder = findImageViewHolderAtPosition(position) ?: return
    holder.populateFile(file)

    val formatToChange = formats[position]
    if (!formatToChange.text.isBlank()) {
      val noteImage = NoteImage(context)
      noteImage.deleteIfExist(noteImage.getFile(note!!.uuid, formatToChange.text))
    }
    formatToChange.text = file.name
    setFormat(formatToChange)
  }

  private fun findTextViewHolderAtPosition(position: Int): FormatTextViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    return if (holder !== null && holder is FormatTextViewHolder) holder else null
  }

  private fun findImageViewHolderAtPosition(position: Int): FormatImageViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    return if (holder !== null && holder is FormatImageViewHolder) holder else null
  }

  private fun findViewHolderAtPositionAggressively(position: Int): RecyclerView.ViewHolder? {
    var holder: RecyclerView.ViewHolder? = formatsView.findViewHolderForAdapterPosition(position)
    if (holder == null) {
      holder = formatsView.findViewHolderForLayoutPosition(position)
      if (holder == null) {
        return null
      }
    }
    return holder
  }

  override fun setNoteColor(color: Int) {
    note!!.color = color
    colorButton.background = CircleDrawable(note!!.color)
  }

  override fun setFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }
    formats[position] = format
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(formats, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(formats, i, i - 1)
      }
    }
    maybeUpdateNoteWithoutSync()
  }

  override fun deleteFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position < 0) {
      return
    }
    focusedFormat = if (focusedFormat == null || focusedFormat!!.uid == format.uid) null else focusedFormat
    formats.removeAt(position)
    adapter.removeItem(position)
    maybeUpdateNoteWithoutSync()
  }

  override fun setFormatChecked(format: Format, checked: Boolean) {
    // do nothing
  }

  override fun createOrChangeToNextFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    val isCheckList = (format.formatType === FormatType.CHECKLIST_UNCHECKED
        || format.formatType === FormatType.CHECKLIST_CHECKED)
    val newPosition = position + 1
    when {
      isCheckList -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(FormatType.CHECKLIST_UNCHECKED))
      newPosition < formats.size -> focus(position + 1)
      else -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(format.formatType))
    }
  }
}
