package com.bijoysingh.quicknote.activities

import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.widget.EditText
import android.widget.GridLayout.VERTICAL
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.external.ITutorialActivity
import com.bijoysingh.quicknote.activities.external.createHint
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.bijoysingh.quicknote.activities.external.maybeAutoExport
import com.bijoysingh.quicknote.activities.sheets.*
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.*
import com.bijoysingh.quicknote.items.EmptyRecyclerItem
import com.bijoysingh.quicknote.items.NoteRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.github.bijoysingh.starter.util.IntentUtils
import java.util.*

class MainActivity : ThemedActivity(), ITutorialActivity, INoteOptionSheetActivity {

  internal lateinit var recyclerView: RecyclerView
  internal lateinit var adapter: NoteAppAdapter

  internal var mode: HomeNavigationState = HomeNavigationState.DEFAULT

  internal lateinit var receiver: BroadcastReceiver
  internal lateinit var executor: SimpleThreadExecutor

  val homeButton: ImageView by bind(R.id.home_button)
  val searchIcon: ImageView by bind(R.id.home_search_button)
  val searchBackButton: ImageView by bind(R.id.search_back_button)
  val searchCloseIcon: ImageView by bind(R.id.search_close_button)
  val deleteTrashIcon: ImageView by bind(R.id.menu_delete_everything)
  val deletesAutomatically: TextView by bind(R.id.deletes_automatically)
  val searchBox: EditText by bind(R.id.search_box)
  val mainToolbar: View by bind(R.id.main_toolbar)
  val searchToolbar: View by bind(R.id.search_toolbar)
  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)

  val deleteToolbar: View by bind(R.id.bottom_delete_toolbar_layout)
  internal var isInSearchMode: Boolean = false
  internal var searchNotes: MutableList<Note>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Migrate to the newer version of the tags
    migrate(this)

    mode = HomeNavigationState.DEFAULT
    executor = SimpleThreadExecutor(1)

    setupRecyclerView()
    setListeners()
    notifyThemeChange()

    val shown = WhatsNewItemsBottomSheet.maybeOpenSheet(this)
    if (shown) {
      markHintShown(TUTORIAL_KEY_NEW_NOTE)
      markHintShown(TUTORIAL_KEY_HOME_SETTINGS)
    }
    showHints()
  }

  fun setListeners() {
    mainToolbar.setOnClickListener {
      setSearchMode(true)
      searchBox.requestFocus()
    }
    deleteTrashIcon.setOnClickListener { AlertBottomSheet.openDeleteTrashSheet(this@MainActivity) }
    searchBackButton.setOnClickListener {
      onBackPressed()
    }
    searchCloseIcon.setOnClickListener { searchBox.setText("") }
    searchBox.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

      }

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        executor.executeNow {
          val items = search(charSequence.toString())
          runOnUiThread { adapter.items = items }
        }
      }

      override fun afterTextChanged(editable: Editable) {

      }
    })
    homeButton.setOnClickListener { HomeNavigationBottomSheet.openSheet(this@MainActivity) }
    primaryFab.setOnClickListener { IntentUtils.startActivity(this@MainActivity, CreateOrEditAdvancedNoteActivity::class.java) }
    secondaryFab.setOnClickListener { HomeNavigationBottomSheet.openSheet(this@MainActivity) }
  }

  fun setupRecyclerView() {
    val staggeredView = UISettingsOptionsBottomSheet.isGridView()
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = userPreferences().get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = userPreferences().get(KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount())

    adapter = NoteAppAdapter(this, staggeredView, isTablet)
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
        .build()
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
          RecyclerView.SCROLL_STATE_DRAGGING -> {
            primaryFab.hide()
            secondaryFab.hide()
          }
          RecyclerView.SCROLL_STATE_IDLE -> {
            primaryFab.show()
            secondaryFab.show()
          }
        }
      }
    })
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    return when {
      isTabletView || isStaggeredView -> StaggeredGridLayoutManager(2, VERTICAL)
      else -> LinearLayoutManager(this)
    }
  }

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    setupData()
  }

  private fun loadNoteByStates(states: Array<String>) {
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
      override fun run(): List<NoteRecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        return sort(Note.db().getByNoteState(states), sorting)
            .map { NoteRecyclerItem(this@MainActivity, it) }
      }

      override fun handle(notes: List<NoteRecyclerItem>) {
        handleNewItems(notes)
      }
    })
  }

  /**
   * Start: Home Navigation Clicks
   */
  fun onHomeClick() {
    mode = HomeNavigationState.DEFAULT
    loadNoteByStates(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
    notifyModeChange()
  }

  fun onFavouritesClick() {
    mode = HomeNavigationState.FAVOURITE
    loadNoteByStates(arrayOf(NoteState.FAVOURITE.name))
    notifyModeChange()
  }

  fun onArchivedClick() {
    mode = HomeNavigationState.ARCHIVED
    loadNoteByStates(arrayOf(NoteState.ARCHIVED.name))
    notifyModeChange()
  }

  fun onTrashClick() {
    mode = HomeNavigationState.TRASH
    loadNoteByStates(arrayOf(NoteState.TRASH.name))
    notifyModeChange()
  }

  fun onLockedClick() {
    mode = HomeNavigationState.LOCKED
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
      override fun run(): List<NoteRecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        return sort(Note.db().getNoteByLocked(true), sorting)
            .map { NoteRecyclerItem(this@MainActivity, it) }
      }

      override fun handle(notes: List<NoteRecyclerItem>) {
        handleNewItems(notes)
      }
    })
    notifyModeChange()
  }

  private fun notifyModeChange() {
    val isTrash = mode === HomeNavigationState.TRASH
    deleteToolbar.visibility = if (isTrash) View.VISIBLE else GONE
  }

  /**
   * End: Home Navigation Clicks
   */

  private fun handleNewItems(notes: List<NoteRecyclerItem>) {
    adapter.clearItems()

    if (notes.isEmpty()) {
      adapter.addItem(EmptyRecyclerItem())
    }

    notes.forEach {
      adapter.addItem(it)
    }
  }

  fun openTag(tag: Tag) {
    mode = HomeNavigationState.TAG
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
      override fun run(): List<NoteRecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        return sort(Note.db().getNoteByTag("%" + tag.uuid + "%"), sorting)
            .map { NoteRecyclerItem(this@MainActivity, it) }
      }

      override fun handle(notes: List<NoteRecyclerItem>) {
        handleNewItems(notes)
      }
    })
    notifyModeChange()
  }

  override fun onResume() {
    super.onResume()
    setupData()
    registerNoteReceiver()
  }

  fun setupData() {
    return when (mode) {
      HomeNavigationState.FAVOURITE -> onFavouritesClick()
      HomeNavigationState.ARCHIVED -> onArchivedClick()
      HomeNavigationState.TRASH -> onTrashClick()
      HomeNavigationState.LOCKED -> onLockedClick()
      HomeNavigationState.DEFAULT -> onHomeClick()
      else -> onHomeClick()
    }
  }

  private fun setSearchMode(mode: Boolean) {
    isInSearchMode = mode
    mainToolbar.visibility = if (isInSearchMode) View.GONE else View.VISIBLE
    searchToolbar.visibility = if (isInSearchMode) View.VISIBLE else View.GONE
    searchBox.setText("")

    if (isInSearchMode) {
      tryOpeningTheKeyboard()
      searchNotes = ArrayList()
      for (item in adapter.items) {
        if (item is NoteRecyclerItem) {
          searchNotes!!.add(item.note)
        }
      }
    } else {
      tryClosingTheKeyboard()
      searchNotes = null
      setupData()
    }
  }

  private fun search(keyword: String): List<RecyclerItem> {
    if (searchNotes == null) {
      return adapter.items
    }

    val notes = ArrayList<RecyclerItem>()
    for (note in searchNotes!!) {
      if (note.search(keyword)) {
        notes.add(NoteRecyclerItem(this, note))
      }
    }
    return notes
  }

  override fun onBackPressed() {
    when {
      isInSearchMode && searchBox.text.toString().isBlank() -> setSearchMode(false)
      isInSearchMode -> searchBox.setText("")
      mode !== HomeNavigationState.DEFAULT -> onHomeClick()
      else -> super.onBackPressed()
    }
  }

  override fun onPause() {
    super.onPause()
    removeOlderClips(this)
    unregisterReceiver(receiver)
  }

  override fun onStop() {
    super.onStop()
    if (getStoragePermissionManager(this).hasAllPermissions()) {
      maybeAutoExport(this)
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()
    val theme = ThemeManager.get(this)

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = theme.get(this, ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)
  }

  private fun registerNoteReceiver() {
    receiver = SyncedNoteBroadcastReceiver {
      setupData()
    }
    registerReceiver(receiver, getNoteIntentFilter())
  }

  /**
   * Start : Tutorial
   */

  override fun showHints(): Boolean {
    when {
      Note.db().count == 0 -> showHint(TUTORIAL_KEY_NEW_NOTE)
      shouldShowHint(TUTORIAL_KEY_NEW_NOTE) -> showHint(TUTORIAL_KEY_NEW_NOTE)
      shouldShowHint(TUTORIAL_KEY_HOME_SETTINGS) -> showHint(TUTORIAL_KEY_HOME_SETTINGS)
      else -> return false
    }

    return true
  }

  override fun shouldShowHint(key: String): Boolean {
    return !userPreferences().get(key, false)
  }

  override fun showHint(key: String) {
    when (key) {
      TUTORIAL_KEY_NEW_NOTE -> createHint(this, primaryFab,
          getString(R.string.tutorial_create_a_new_note),
          getString(R.string.main_no_notes_hint))
      TUTORIAL_KEY_HOME_SETTINGS -> createHint(this, secondaryFab,
          getString(R.string.tutorial_home_menu),
          getString(R.string.tutorial_home_menu_subtitle))
    }
    markHintShown(key)
  }

  override fun markHintShown(key: String) {
    userPreferences().put(key, true)
  }

  /**
   * End : Tutorial
   */

  companion object {
    const val TUTORIAL_KEY_NEW_NOTE = "TUTORIAL_KEY_NEW_NOTE"
    const val TUTORIAL_KEY_HOME_SETTINGS = "TUTORIAL_KEY_HOME_SETTINGS"
  }


  /**
   * Start : INoteOptionSheetActivity Functions
   */

  override fun updateNote(note: Note) {
    note.save(this)
    setupData()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
    setupData()
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    note.deleteOrMoveToTrash(this)
    setupData()
  }

  override fun notifyTagsChanged(note: Note) {
    setupData()
  }

  override fun getSelectMode(note: Note): String {
    return mode.name
  }

  override fun notifyResetOrDismiss() {
    setupData()
  }

  override fun lockedContentIsHidden() = true

  /**
   * End : INoteOptionSheetActivity
   */
}
