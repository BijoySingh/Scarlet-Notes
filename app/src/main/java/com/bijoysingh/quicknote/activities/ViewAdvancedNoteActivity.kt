package com.bijoysingh.quicknote.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.NoteAdvancedActivityBottomSheet
import com.bijoysingh.quicknote.activities.sheets.NoteSettingsOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.bijoysingh.quicknote.recyclerview.FormatAdapter
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import android.support.v7.widget.LinearLayoutManager
import com.bijoysingh.quicknote.recyclerview.EmptyFormatHolder


const val INTENT_KEY_NOTE_ID = "NOTE_ID"
open class ViewAdvancedNoteActivity : ThemedActivity() {

  var focusedFormat: Format? = null
  protected var note: Note? = null

  protected lateinit var context: Context
  protected lateinit var store: DataStore
  protected lateinit var adapter: FormatAdapter
  protected lateinit var formats: MutableList<Format>
  protected lateinit var formatsView: RecyclerView

  val toolbar: View by bind(R.id.toolbar)
  val formatToolbar: View by bind(R.id.format_toolbar)
  val markdownToolbar: View by bind(R.id.markdown_toolbar)

  val rootView: View by bind(R.id.root_layout)
  val backButton: ImageView by bind(R.id.back_button)
  val actionCopy: ImageView by bind(R.id.copy_button)
  val actionDelete: ImageView by bind(R.id.delete_button)
  val actionShare: ImageView by bind(R.id.share_button)
  val actionEdit: ImageView by bind(R.id.edit_button)
  val actionDone: ImageView by bind(R.id.done_button)
  val actionOptions: ImageView by bind(R.id.note_options_button)
  val colorButton: ImageView by bind(R.id.color_button)

  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_advanced_note)
    context = this
    store = DataStore.get(context)

    var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
    }
    if (noteId != 0) {
      note = Note.db(this).getByID(noteId)
    }
    if (note === null) {
      note = genEmptyNote(NoteSettingsOptionsBottomSheet.genDefaultColor(store))
    }

    setRecyclerView()
    setToolbars()
    setEditMode()
    notifyThemeChange()
  }

  override fun onResume() {
    super.onResume()
    onResumeAction()
  }

  protected open fun onResumeAction() {
    note = Note.db(this).getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
    if (note == null) {
      finish()
      return
    }
    setNote()
  }

  protected open fun setEditMode() {
    setEditMode(editModeValue)
    formatsView.setBackgroundColor(ThemeManager.get(this).get(this, ThemeColorType.BACKGROUND))
  }

  protected fun setEditMode(mode: Boolean) {
    resetBundle()
    setNote()

    actionEdit.visibility = if (mode) GONE else VISIBLE
    actionDone.visibility = if (mode) VISIBLE else GONE
    toolbar.visibility = if (mode) VISIBLE else GONE
    primaryFab.visibility = if (mode) GONE else VISIBLE
    secondaryFab.visibility = if (mode) GONE else VISIBLE
    markdownToolbar.visibility = GONE
  }

  private fun resetBundle() {
    val bundle = Bundle()
    bundle.putBoolean(FormatTextViewHolder.KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, store.get(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, ThemeManager.get(this).isNightTheme())
    bundle.putInt(TextSizeBottomSheet.KEY_TEXT_SIZE, TextSizeBottomSheet.getDefaultTextSize(store))
    adapter.setExtra(bundle)
  }

  protected open fun setNote() {
    setNoteColor(note!!.color)
    adapter.clearItems()
    formats = note!!.formats
    adapter.addItems(formats)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }
  }

  private fun maybeAddTags() {
    val tags = note!!.getTags(context)
    val tagLabel = Note.getTagString(tags)
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
    if (!editModeValue) {
      formatsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        private fun onShow() {
          primaryFab.show()
          secondaryFab.show()
        }

        private fun onHide() {
          primaryFab.hide()
          secondaryFab.hide()
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
          super.onScrollStateChanged(recyclerView, newState)
          when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING -> {
              onHide()
            }
            RecyclerView.SCROLL_STATE_IDLE -> {
              onShow()
            }
          }
        }
      })
    }
  }

  open fun setFormat(format: Format) {
    // do nothing
  }

  open fun moveFormat(from: Int, to: Int) {
    // do nothing
  }

  open fun deleteFormat(format: Format) {
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
    updateNote()
  }

  private fun setToolbars() {
    setBottomToolbar()
    actionDelete.setOnClickListener {
      moveItemToTrashOrDelete(note!!)
    }
    actionCopy.setOnClickListener { note!!.copy(context) }
    backButton.setOnClickListener { onBackPressed() }
    actionOptions.setOnClickListener { openMoreOptions() }
    actionShare.setOnClickListener { note!!.share(context) }
    actionEdit.setOnClickListener { openEditor() }
    actionDone.setOnClickListener { onBackPressed() }
    primaryFab.setOnClickListener { openEditor() }
    secondaryFab.setOnClickListener { openMoreOptions() }
    setTopToolbar()
    notifyToolbarColor()
  }

  fun openMoreOptions() {
    NoteAdvancedActivityBottomSheet.openSheet(
        this@ViewAdvancedNoteActivity,
        note!!,
        editModeValue)
  }

  fun openEditor() {
    note!!.startEditActivity(context)
  }

  protected open fun notifyToolbarColor() {
    val theme = ThemeManager.get(this)
    val toolbarIconColor = theme.get(context, ThemeColorType.TOOLBAR_ICON)
    backButton.setColorFilter(toolbarIconColor)
    actionCopy.setColorFilter(toolbarIconColor)
    actionDelete.setColorFilter(toolbarIconColor)
    actionShare.setColorFilter(toolbarIconColor)
    actionEdit.setColorFilter(toolbarIconColor)
    actionDone.setColorFilter(toolbarIconColor)
    actionOptions.setColorFilter(toolbarIconColor)

    val backgroundColor = theme.get(context, ThemeColorType.BACKGROUND)
    rootView.setBackgroundColor(backgroundColor)
    formatsView.setBackgroundColor(backgroundColor)

    resetBundle()
    adapter.notifyDataSetChanged()
    setSystemTheme()
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

  protected fun maybeSaveNoteWithSync() {
    maybeSaveNote(true)
  }

  protected fun maybeSaveNote(sync: Boolean) {
    if (note!!.formats.isEmpty() && note!!.isUnsaved) {
      return
    }
    if (sync)
      note!!.save(context)
    else
      note!!.saveWithoutSync(context)
  }

  private fun updateNote() {
    note!!.description = Format.getNote(formats)
    maybeSaveNoteWithSync()
  }

  fun moveItemToTrashOrDelete(note: Note) {
    if (note.noteState === NoteState.TRASH) {
      note.delete(this)
    } else {
      markItem(note, NoteState.TRASH)
    }
    finish()
  }


  fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
  }

  fun notifyNoteChange() {

  }

  fun notifyTagsChanged() {
    setNote()
  }

  protected fun getFormatIndex(format: Format): Int {
    var position = 0
    for (fmt in formats) {
      if (fmt.uid == format.uid) {
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
}
