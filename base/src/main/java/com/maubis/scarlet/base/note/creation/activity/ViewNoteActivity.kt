package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.core.note.isUnsaved
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.actions.NoteOptionsBottomSheet
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.formats.FormatAdapter
import com.maubis.scarlet.base.note.formats.IFormatRecyclerViewActivity
import com.maubis.scarlet.base.note.formats.getFormatControllerItems
import com.maubis.scarlet.base.note.formats.recycler.KEY_EDITABLE
import com.maubis.scarlet.base.note.formats.recycler.KEY_NOTE_COLOR
import com.maubis.scarlet.base.settings.sheet.NoteSettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.TextSizeBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet.Companion.useNoteColorAsBackground
import com.maubis.scarlet.base.support.ui.*
import com.maubis.scarlet.base.support.ui.ColorUtil.darkerColor
import com.maubis.scarlet.base.support.utils.bind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


const val INTENT_KEY_NOTE_ID = "NOTE_ID"
const val INTENT_KEY_DISTRACTION_FREE = "DISTRACTION_FREE"

open class ViewAdvancedNoteActivity : ThemedActivity(), INoteOptionSheetActivity, IFormatRecyclerViewActivity {

  var focusedFormat: Format? = null
  protected var note: Note? = null

  protected lateinit var context: Context
  protected lateinit var adapter: FormatAdapter
  protected lateinit var formats: MutableList<Format>
  protected lateinit var formatsView: RecyclerView
  protected var isDistractionFree: Boolean = false

  val creationFinished = AtomicBoolean(false)

  val topToolbar: View by bind(R.id.top_toolbar_layout)
  val toolbar: View by bind(R.id.toolbar)
  val formatToolbar: View by bind(R.id.format_toolbar)
  val markdownToolbar: View by bind(R.id.markdown_toolbar)

  val rootView: View by bind(R.id.root_layout)
  val backButton: ImageView by bind(R.id.back_button)
  val actionUndo: ImageView by bind(R.id.undo_button)
  val actionRedo: ImageView by bind(R.id.redo_button)
  val actionCopy: ImageView by bind(R.id.copy_button)
  val actionDelete: ImageView by bind(R.id.delete_button)
  val actionShare: ImageView by bind(R.id.share_button)
  val actionDone: ImageView by bind(R.id.done_button)
  val colorButton: ImageView by bind(R.id.color_button)

  val toolbarBottom: View by bind(R.id.toolbar_bottom)
  val toolbarOption: ImageView by bind(R.id.toolbar_icon_options)
  val toolbarEdit: ImageView by bind(R.id.toolbar_icon_edit_note)
  val toolbarTimestamp: TextView by bind(R.id.toolbar_timestamp)

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_advanced_note)
    context = this
    isDistractionFree = intent.getBooleanExtra(INTENT_KEY_DISTRACTION_FREE, false)

    setRecyclerView()

    GlobalScope.launch(Dispatchers.IO) {
      var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
      if (noteId == 0 && savedInstanceState != null) {
        noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
      }
      if (noteId != 0) {
        note = notesDb.getByID(noteId)
      }
      if (note === null) {
        note = NoteBuilder().emptyNote(NoteSettingsOptionsBottomSheet.genDefaultColor())
      }
      GlobalScope.launch(Dispatchers.Main) {
        setToolbars()
        setEditMode()
        notifyThemeChange()
        onCreationFinished()
      }
      creationFinished.set(true)
    }
  }

  override fun onResume() {
    super.onResume()
    CoreConfig.instance.startListener(this)

    if (!creationFinished.get()) {
      return
    }
    onResumeAction()
    notifyThemeChange()
  }

  protected open fun onCreationFinished() {

  }

  protected open fun onResumeAction() {
    GlobalScope.launch(Dispatchers.IO) {
      note = notesDb.getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
      when {
        note == null -> finish()
        else -> GlobalScope.launch(Dispatchers.Main) { setNote() }
      }
    }
  }

  protected open fun setEditMode() {
    setEditMode(editModeValue)
    formatsView.setBackgroundColor(CoreConfig.instance.themeController().get(ThemeColorType.BACKGROUND))
  }

  protected fun setEditMode(mode: Boolean) {
    resetBundle()
    setNote()

    val visibleInEditMode = if (mode) VISIBLE else GONE
    actionDone.visibility = visibleInEditMode
    actionUndo.visibility = visibleInEditMode
    actionRedo.visibility = visibleInEditMode
    toolbar.visibility = visibleInEditMode

    val visibleInNormalMode = if (mode || isDistractionFree) GONE else VISIBLE
    toolbarBottom.visibility = visibleInNormalMode
    markdownToolbar.visibility = GONE
  }

  private fun startDistractionFreeMode() {
    topToolbar.visibility = GONE
    toolbarBottom.visibility = GONE

    var uiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    if (Build.VERSION.SDK_INT >= 19) {
      uiVisibility = (uiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
    window.decorView.systemUiVisibility = uiVisibility
  }

  private fun resetBundle() {
    val bundle = Bundle()
    bundle.putBoolean(KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, CoreConfig.instance.store().get(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, CoreConfig.instance.themeController().isNightTheme())
    bundle.putInt(TextSizeBottomSheet.KEY_TEXT_SIZE, TextSizeBottomSheet.getDefaultTextSize())
    bundle.putInt(KEY_NOTE_COLOR, note!!.color)
    bundle.putString(INTENT_KEY_NOTE_ID, note!!.uuid)
    adapter.setExtra(bundle)
  }

  protected open fun setNote() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    setNoteColor(currentNote.color)
    adapter.clearItems()

    if (isDistractionFree) {
      adapter.addItem(Format(FormatType.SEPARATOR))
    }

    formats = when (editModeValue) {
      true -> currentNote.getFormats()
      false -> currentNote.getSmartFormats()
    }.toMutableList()
    adapter.addItems(formats)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }

    toolbarTimestamp.setText(currentNote.getDisplayTime())
  }

  private fun maybeAddTags() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val tagLabel = currentNote.getTagString()
    if (tagLabel.isEmpty()) {
      return
    }

    val format = Format(FormatType.TAG, tagLabel)
    adapter.addItem(format)
  }

  private fun maybeAddEmptySpace() {
    adapter.addItem(Format(FormatType.EMPTY))
  }

  private fun setRecyclerView() {
    adapter = FormatAdapter(this)
    formatsView = RecyclerViewBuilder(this)
        .setAdapter(adapter)
        .setView(this, R.id.advanced_note_recycler)
        .build()
  }

  open fun setFormat(format: Format) {
    // do nothing
  }

  open fun createOrChangeToNextFormat(format: Format) {
    // do nothing
  }

  open fun setFormatChecked(format: Format, checked: Boolean) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }
    format.formatType = if (checked) FormatType.CHECKLIST_CHECKED else FormatType.CHECKLIST_UNCHECKED
    formats[position] = format
    adapter.updateItem(format, position)
    updateNoteForChecked()
  }

  private fun setToolbars() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    setBottomToolbar()
    actionDelete.setOnClickListener {
      moveItemToTrashOrDelete(currentNote)
    }
    actionCopy.setOnClickListener { currentNote.copy(context) }
    backButton.setOnClickListener { onBackPressed() }
    actionShare.setOnClickListener { currentNote.share(context) }
    actionDone.setOnClickListener { onBackPressed() }
    toolbarOption.setOnClickListener { openMoreOptions() }
    toolbarEdit.setOnClickListener { openEditor() }
    setTopToolbar()
    notifyToolbarColor()

    if (isDistractionFree) {
      startDistractionFreeMode()
    }
  }

  fun openMoreOptions() {
    NoteOptionsBottomSheet.openSheet(this@ViewAdvancedNoteActivity, note!!)
  }

  fun openEditor() {
    note!!.openEdit(context)
  }

  protected open fun notifyToolbarColor() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val theme = CoreConfig.instance.themeController()

    val backgroundColor: Int
    val toolbarIconColor: Int
    val statusBarColor: Int
    when {
      !useNoteColorAsBackground -> {
        backgroundColor = theme.get(ThemeColorType.BACKGROUND)
        toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
        statusBarColor = backgroundColor
      }
      ColorUtil.isLightColored(currentNote.color) -> {
        backgroundColor = currentNote.color
        toolbarIconColor = theme.get(context, Theme.LIGHT, ThemeColorType.TOOLBAR_ICON)
        statusBarColor = darkerColor(currentNote.color)
      }
      else -> {
        backgroundColor = currentNote.color
        toolbarIconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        statusBarColor = darkerColor(currentNote.color)
      }
    }

    backButton.setColorFilter(toolbarIconColor)
    actionRedo.setColorFilter(toolbarIconColor)
    actionUndo.setColorFilter(toolbarIconColor)
    actionCopy.setColorFilter(toolbarIconColor)
    actionDelete.setColorFilter(toolbarIconColor)
    actionShare.setColorFilter(toolbarIconColor)
    actionDone.setColorFilter(toolbarIconColor)

    setSystemTheme(statusBarColor)
    rootView.setBackgroundColor(backgroundColor)
    formatsView.setBackgroundColor(backgroundColor)

    toolbarEdit.setColorFilter(toolbarIconColor)
    toolbarOption.setColorFilter(toolbarIconColor)
    toolbarTimestamp.setTextColor(toolbarIconColor)
    toolbarBottom.setBackgroundColor(backgroundColor)

    resetBundle()
    adapter.notifyDataSetChanged()
  }

  protected open fun setBottomToolbar() {
    toolbar.visibility = GONE
    markdownToolbar.visibility = GONE
  }

  protected open fun setTopToolbar() {
    val colorButtonClicker = findViewById<View>(R.id.color_button_clicker)
    colorButtonClicker.visibility = GONE
  }

  protected open fun setNoteColor(color: Int) {
    colorButton.background = CircleDrawable(note!!.color)
  }

  protected fun maybeSaveNote(sync: Boolean) {
    if (note!!.getFormats().isEmpty() && note!!.isUnsaved()) {
      return
    }
    note!!.updateTimestamp = Calendar.getInstance().timeInMillis
    when (sync) {
      true -> note!!.save(context)
      false -> note!!.saveWithoutSync(context)
    }
  }

  private fun updateNoteForChecked() {
    note!!.description = FormatBuilder().getDescription(formats.sorted())
    setNote()
    maybeSaveNote(true)
  }

  fun notifyNoteChange() {
    notifyToolbarColor()
  }

  protected fun getFormatIndex(format: Format): Int = getFormatIndex(format.uid)

  protected fun getFormatIndex(formatUid: Int): Int {
    var position = 0
    for (fmt in formats) {
      if (fmt.uid == formatUid) {
        return position
      }
      position++
    }
    return -1
  }

  override fun notifyThemeChange() {
    notifyToolbarColor()
  }

  public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
    super.onSaveInstanceState(savedInstanceState)
    if (savedInstanceState == null) {
      return
    }
    savedInstanceState.putInt(INTENT_KEY_NOTE_ID, if (note == null || note!!.uid == null) 0 else note!!.uid)
  }

  companion object {
    const val HANDLER_UPDATE_TIME = 1000

    fun getIntent(context: Context, note: Note): Intent {
      val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return intent
    }
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */

  override fun updateNote(note: Note) {
    note.save(this)
    notifyNoteChange()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    note.softDelete(context)
    finish()
  }

  override fun notifyTagsChanged(note: Note) {
    setNote()
  }

  override fun getSelectMode(note: Note): String {
    return NoteState.DEFAULT.name
  }

  override fun notifyResetOrDismiss() {
    finish()
  }

  override fun lockedContentIsHidden() = false

  /**
   * End : INoteOptionSheetActivity
   */


  /**
   * Start : IFormatRecyclerView Functions
   */

  override fun context(): Context {
    return this
  }

  override fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
    return getFormatControllerItems()
  }

  override fun deleteFormat(format: Format) {
    // do nothing
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    // do nothing
  }

  /**
   * End : IFormatRecyclerView
   */

}
