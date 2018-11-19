package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.format.*
import com.maubis.scarlet.base.core.note.*
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.note.creation.sheet.NoteFormatOptionsBottomSheet
import com.maubis.scarlet.base.note.creation.sheet.NoteMarkdownOptionsBottomSheet
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.formats.recycler.FormatImageViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatTextViewHolder
import com.maubis.scarlet.base.note.saveToSync
import com.maubis.scarlet.base.settings.sheet.NoteColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.utils.bind
import com.maubis.scarlet.base.support.database.foldersDB
import com.maubis.scarlet.base.support.recycler.SimpleItemTouchHelper
import com.maubis.scarlet.base.support.ui.CircleDrawable
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.ColorUtil.darkerColor
import com.maubis.scarlet.base.support.ui.ColorUtil.luminantColor
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeColorType
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

open class CreateNoteActivity : ViewAdvancedNoteActivity() {

  private var active = false
  private var maxUid = 0
  private var toolbarMode: ToolbarMode = ToolbarMode.FORMAT

  private var historyIndex = 0
  private var historySize = 0L

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

  val history: MutableList<Note> = emptyList<Note>().toMutableList()

  override val editModeValue: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTouchListener()
    startHandler()
    history.add(NoteBuilder().copy(note!!))
    setFolderFromIntent()
    notifyHistoryIcons()
  }

  override fun setEditMode() {
    setEditMode(editModeValue)
  }

  private fun setFolderFromIntent() {
    if (intent === null) {
      return
    }
    val folderUuid = intent.getStringExtra(INTENT_KEY_FOLDER)
    if (folderUuid === null || folderUuid.isBlank()) {
      return
    }
    val folder = foldersDB.getByUUID(folderUuid)
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
    actionUndo.setOnClickListener {
      historyIndex = if (historyIndex == 0) 0 else (historyIndex - 1)
      note = NoteBuilder().copy(history.get(historyIndex))
      notifyHistoryIcons()
      setNote()
    }
    actionRedo.setOnClickListener {
      val maxHistoryIndex = history.size - 1
      historyIndex = if (historyIndex == maxHistoryIndex) maxHistoryIndex else (historyIndex + 1)
      note = NoteBuilder().copy(history.get(historyIndex))
      notifyHistoryIcons()
      setNote()
    }
    actionDelete.visibility = GONE
    actionShare.visibility = GONE
    actionCopy.visibility = GONE

    val colorButtonClicker = findViewById<View>(R.id.color_button_clicker)
    colorButtonClicker.setOnClickListener {
      NoteColorPickerBottomSheet.openSheet(
          this@CreateNoteActivity,
          object : NoteColorPickerBottomSheet.ColorPickerController {
            override fun onColorSelected(note: Note, color: Int) = setNoteColor(color)
            override fun getNote(): Note = note!!
          })
    }
  }

  override fun notifyToolbarColor() {
    super.notifyToolbarColor()
    val theme = CoreConfig.instance.themeController()

    val toolbarIconColor: Int
    val toolbarBackgroundColor: Int
    when {
      !UISettingsOptionsBottomSheet.useNoteColorAsBackground -> {
        toolbarBackgroundColor = theme.get(ThemeColorType.TOOLBAR_BACKGROUND)
        toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
      }
      ColorUtil.isLightColored(note!!.color) -> {
        toolbarBackgroundColor = luminantColor(note!!.color, 0.35f)
        toolbarIconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
      }
      else -> {
        toolbarBackgroundColor = darkerColor(note!!.color)
        toolbarIconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
      }
    }

    toolbar.setBackgroundColor(toolbarBackgroundColor)
    markdownToolbar.setBackgroundColor(toolbarBackgroundColor)

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

    chevronLeft.alpha = 0.7f
    chevronLeft.setColorFilter(toolbarIconColor)
    chevronRight.alpha = 0.7f
    chevronRight.setColorFilter(toolbarIconColor)
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
    val vNote = note!!
    val vLastNoteInstance = history.getOrNull(historyIndex) ?: note!!

    vNote.description = FormatBuilder().getDescription(formats)

    // Ignore update if nothing changed. It allows for one undo per few seconds
    if (vNote.isEqual(vLastNoteInstance)) {
      return
    }

    addNoteToHistory(NoteBuilder().copy(vNote))
    vNote.updateTimestamp = Calendar.getInstance().timeInMillis
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
    notifyHistoryIcons()
  }

  private fun notifyHistoryIcons() {
    actionRedo.alpha = when (historyIndex != history.size - 1) {
      true -> 1.0f
      false -> 0.4f
    }
    actionUndo.alpha = when (historyIndex == 0) {
      true -> 0.4f
      false -> 1.0f
    }
  }

  private fun startHandler() {
    val handler = Handler()
    handler.postDelayed(object : Runnable {
      override fun run() {
        if (active) {
          maybeUpdateNoteWithoutSync()
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
      deleteIfExist(noteImage.getFile(note!!.uuid, formatToChange.text))
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
    notifyToolbarColor()
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

  companion object {
    private const val INTENT_KEY_FOLDER = "key_folder"

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
