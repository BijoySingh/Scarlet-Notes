package com.bijoysingh.quicknote.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.ImageView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.NoteOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.NoteSettingsOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.*
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.bijoysingh.quicknote.recyclerview.FormatAdapter
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import java.util.*
import kotlin.math.roundToInt


const val INTENT_KEY_NOTE_ID = "NOTE_ID"
const val INTENT_KEY_DISTRACTION_FREE = "DISTRACTION_FREE"

open class ViewAdvancedNoteActivity : ThemedActivity(), INoteOptionSheetActivity {

  var focusedFormat: Format? = null
  protected var note: Note? = null

  protected lateinit var context: Context
  protected lateinit var adapter: FormatAdapter
  protected lateinit var formats: MutableList<Format>
  protected lateinit var formatsView: RecyclerView
  protected var isDistractionFree: Boolean = false

  val topToolbar: View by bind(R.id.top_toolbar_layout)
  val toolbar: View by bind(R.id.toolbar)
  val formatToolbar: View by bind(R.id.format_toolbar)
  val markdownToolbar: View by bind(R.id.markdown_toolbar)

  val rootView: View by bind(R.id.root_layout)
  val backButton: ImageView by bind(R.id.back_button)
  val actionCopy: ImageView by bind(R.id.copy_button)
  val actionDelete: ImageView by bind(R.id.delete_button)
  val actionShare: ImageView by bind(R.id.share_button)
  val actionDone: ImageView by bind(R.id.done_button)
  val colorButton: ImageView by bind(R.id.color_button)

  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_advanced_note)
    context = this

    var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
    }
    if (noteId != 0) {
      note = Note.db().getByID(noteId)
    }
    if (note === null) {
      note = genEmptyNote(NoteSettingsOptionsBottomSheet.genDefaultColor())
    }
    isDistractionFree = intent.getBooleanExtra(INTENT_KEY_DISTRACTION_FREE, false)

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
    note = Note.db().getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
    if (note == null) {
      finish()
      return
    }
    setNote()
  }

  protected open fun setEditMode() {
    setEditMode(editModeValue)
    formatsView.setBackgroundColor(appTheme().get(ThemeColorType.BACKGROUND))
  }

  protected fun setEditMode(mode: Boolean) {
    resetBundle()
    setNote()

    actionDone.visibility = if (mode) VISIBLE else GONE
    toolbar.visibility = if (mode) VISIBLE else GONE
    primaryFab.visibility = if (mode || isDistractionFree) GONE else VISIBLE
    secondaryFab.visibility = if (mode|| isDistractionFree) GONE else VISIBLE
    markdownToolbar.visibility = GONE
  }

  private fun startDistractionFreeMode() {
    primaryFab.hide()
    secondaryFab.hide()
    topToolbar.visibility = GONE

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
    bundle.putBoolean(FormatTextViewHolder.KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, userPreferences().get(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, appTheme().isNightTheme())
    bundle.putInt(TextSizeBottomSheet.KEY_TEXT_SIZE, TextSizeBottomSheet.getDefaultTextSize())
    bundle.putString(INTENT_KEY_NOTE_ID, note!!.uuid)
    adapter.setExtra(bundle)
  }

  protected open fun setNote() {
    setNoteColor(note!!.color)
    adapter.clearItems()

    if (isDistractionFree) {
      adapter.addItem(Format(FormatType.SEPARATOR))
    }

    formats = note!!.getFormats().toMutableList()
    adapter.addItems(formats)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }
  }

  private fun maybeAddTags() {
    val tagLabel = note!!.getTagString()
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
          if (isDistractionFree) {
            return
          }
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

  open fun moveFormat(fromPosition: Int, toPosition: Int) {
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
    updateNoteForChecked()
  }

  private fun setToolbars() {
    setBottomToolbar()
    actionDelete.setOnClickListener {
      moveItemToTrashOrDelete(note!!)
    }
    actionCopy.setOnClickListener { note!!.copy(context) }
    backButton.setOnClickListener { onBackPressed() }
    actionShare.setOnClickListener { note!!.share(context) }
    actionDone.setOnClickListener { onBackPressed() }
    primaryFab.setOnClickListener { openEditor() }
    secondaryFab.setOnClickListener { openMoreOptions() }
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
    val theme = appTheme()
    val toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    backButton.setColorFilter(toolbarIconColor)
    actionCopy.setColorFilter(toolbarIconColor)
    actionDelete.setColorFilter(toolbarIconColor)
    actionShare.setColorFilter(toolbarIconColor)
    actionDone.setColorFilter(toolbarIconColor)

    val backgroundColor = theme.get(ThemeColorType.BACKGROUND)
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
    note!!.description = Format.getNote(formats.sorted())
    setNote()
    maybeSaveNote(true)
  }

  fun notifyNoteChange() {

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
    note.deleteOrMoveToTrash(context)
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

}
