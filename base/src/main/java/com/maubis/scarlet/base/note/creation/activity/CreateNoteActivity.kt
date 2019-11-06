package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppImageStorage
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.core.note.isEqual
import com.maubis.scarlet.base.core.note.isUnsaved
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.creation.specs.NoteCreationBottomBar
import com.maubis.scarlet.base.note.creation.specs.NoteCreationTopBar
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.formats.recycler.FormatImageViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatTextViewHolder
import com.maubis.scarlet.base.note.saveToSync
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.ColorPickerDefaultController
import com.maubis.scarlet.base.support.recycler.SimpleItemTouchHelper
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.android.synthetic.main.activity_advanced_note.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

open class CreateNoteActivity : ViewAdvancedNoteActivity() {

  private var active = false
  private var maxUid = 0

  private var historyIndex = 0
  private var historySize = 0L
  private var historyModified = false
  private val history: MutableList<Note> = emptyList<Note>().toMutableList()

  override val editModeValue: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTouchListener()
    startHandler()
  }

  override fun onCreationFinished() {
    super.onCreationFinished()
    history.add(NoteBuilder().copy(note!!))
    setFolderFromIntent()
  }

  private fun setFolderFromIntent() {
    if (intent === null) {
      return
    }
    val folderUuid = intent.getStringExtra(INTENT_KEY_FOLDER)
    if (folderUuid === null || folderUuid.isBlank()) {
      return
    }
    val folder = foldersDb.getByUUID(folderUuid)
    if (folder === null) {
      return
    }
    note!!.folder = folder.uuid
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
    when {
      isEmpty -> {
        addEmptyItem(0, FormatType.HEADING)
        addDefaultItem()
      }
      !formats[0].text.startsWith("# ") &&
        formats[0].formatType !== FormatType.HEADING
        && formats[0].formatType !== FormatType.IMAGE -> {
        addEmptyItem(0, FormatType.HEADING)
      }
    }
    focus(0)
  }

  protected open fun addDefaultItem() {
    addEmptyItem(FormatType.TEXT)
  }

  override fun notifyToolbarColor() {
    super.notifyToolbarColor()
    setBottomToolbar()
  }

  override fun setTopToolbar() {
    lithoTopToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    lithoTopToolbar.addView(
      LithoView.create(
        componentContext,
        NoteCreationTopBar.create(componentContext).build()))
  }

  override fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.removeAllViews()
    lithoBottomToolbar.addView(
      LithoView.create(
        componentContext,
        NoteCreationBottomBar.create(componentContext)
          .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
          .build()))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
      override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
        if (imageFile == null) {
          return
        }

        val targetFile = sAppImageStorage.renameOrCopy(note!!, imageFile)
        val index = getFormatIndex(type)
        triggerImageLoaded(index, targetFile)
      }

      override fun onImagePickerError(exception: Exception, source: EasyImage.ImageSource, type: Int) {
        maybeThrow(this@CreateNoteActivity, exception)
      }
    })
  }

  override fun onPause() {
    super.onPause()
    active = false
    maybeUpdateNoteWithoutSync()
    val destroyed = destroyIfNeeded()
    if (!destroyed && !note!!.disableBackup) {
      note!!.saveToSync(this)
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
    val currentNote = note
    if (currentNote === null || !formatsInitialised.get()) {
      return
    }

    val vLastNoteInstance = history.getOrNull(historyIndex) ?: currentNote
    currentNote.description = FormatBuilder().getSmarterDescription(formats)

    // Ignore update if nothing changed. It allows for one undo per few seconds
    when {
      !historyModified && currentNote.isEqual(vLastNoteInstance) -> return
      !historyModified -> addNoteToHistory(NoteBuilder().copy(currentNote))
      else -> historyModified = false
    }
    currentNote.updateTimestamp = Calendar.getInstance().timeInMillis
    maybeSaveNote(false)
  }

  @Synchronized
  private fun addNoteToHistory(note: Note) {
    while (historyIndex != history.size - 1) {
      history.removeAt(historyIndex)
    }

    history.add(note)
    historySize += note.description.length
    historyIndex += 1

    // 0.5MB limit on history
    if (historySize >= 1024 * 512 || history.size >= 15) {
      val item = history.removeAt(0)
      historySize -= item.description.length
      historyIndex -= 1
    }
  }

  private fun startHandler() {
    val handler = Handler()
    handler.postDelayed(object : Runnable {
      override fun run() {
        if (active) {
          maybeUpdateNoteWithoutSync()
          fullScreenView()
          handler.postDelayed(this, HANDLER_UPDATE_TIME.toLong())
        }
      }
    }, HANDLER_UPDATE_TIME.toLong())
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
    formatsView.layoutManager?.scrollToPosition(newPosition)
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
      val noteImage = sAppImageStorage
      deleteIfExist(noteImage.getFile(note!!.uuid, formatToChange.text))
    }
    formatToChange.text = file.name
    setFormat(formatToChange)
  }

  fun onHistoryClick(undo: Boolean) {
    when (undo) {
      true -> {
        historyIndex = if (historyIndex == 0) 0 else (historyIndex - 1)
        note = NoteBuilder().copy(history.get(historyIndex))
        setNote()
        historyModified = true
      }
      false -> {
        val maxHistoryIndex = history.size - 1
        historyIndex = if (historyIndex == maxHistoryIndex) maxHistoryIndex else (historyIndex + 1)
        note = NoteBuilder().copy(history.get(historyIndex))
        setNote()
        historyModified = true
      }
    }
  }

  fun onColorChangeClick() {
    val config = ColorPickerDefaultController(
      title = R.string.choose_note_color,
      colors = listOf(resources.getIntArray(R.array.bright_colors), resources.getIntArray(R.array.bright_colors_accent)),
      selectedColor = note!!.color,
      onColorSelected = { color ->
        setNoteColor(color)
      }
    )
    com.maubis.scarlet.base.support.sheets.openSheet(this, ColorPickerBottomSheet().apply { this.config = config })
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
    if (lastKnownNoteColor == color) {
      return
    }
    note!!.color = color
    notifyToolbarColor()
    lastKnownNoteColor = color
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

    val isCheckList =
      (format.formatType === FormatType.CHECKLIST_UNCHECKED
        || format.formatType === FormatType.CHECKLIST_CHECKED)
    val newPosition = position + 1
    when {
      isCheckList -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(FormatType.CHECKLIST_UNCHECKED))
      newPosition < formats.size -> focus(position + 1)
      else -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(format.formatType))
    }
  }

  companion object {
    const val INTENT_KEY_FOLDER = "key_folder"

    fun getNewNoteIntent(
      context: Context,
      folder: String = ""): Intent {
      val intent = Intent(context, CreateNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folder)
      return intent
    }

    fun getNewChecklistNoteIntent(
      context: Context,
      folder: String = ""): Intent {
      val intent = Intent(context, CreateListNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folder)
      return intent
    }
  }
}
