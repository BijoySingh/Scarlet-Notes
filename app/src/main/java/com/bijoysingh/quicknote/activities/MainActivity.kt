package com.bijoysingh.quicknote.activities

import android.content.BroadcastReceiver
import android.content.res.ColorStateList
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
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.*
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.items.EmptyRecyclerItem
import com.bijoysingh.quicknote.items.NoteRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.github.bijoysingh.starter.util.IntentUtils
import java.util.*

class MainActivity : ThemedActivity() {

  internal lateinit var recyclerView: RecyclerView
  internal lateinit var adapter: NoteAppAdapter

  internal var mode: HomeNavigationState = HomeNavigationState.DEFAULT

  internal lateinit var receiver: BroadcastReceiver
  internal lateinit var store: DataStore
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
    store = DataStore.get(this)
    executor = SimpleThreadExecutor(1)

    setupRecyclerView()
    setListeners()
    registerNoteReceiver()
    notifyThemeChange()
  }

  fun setListeners() {
    searchIcon.setOnClickListener {
      setSearchMode(true)
      searchBox.requestFocus()
    }
    deleteTrashIcon.setOnClickListener { AlertBottomSheet.openDeleteTrashSheet(this@MainActivity) }
    searchBackButton.setOnClickListener { onBackPressed() }
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
    val staggeredView = store.get(KEY_LIST_VIEW, false)
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = store.get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = store.get(KEY_MARKDOWN_HOME_ENABLED, false)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount(store))

    adapter = NoteAppAdapter(this, staggeredView, isTablet)
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
        .build()
    recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
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

  fun setLayoutMode(staggered: Boolean) {
    store.put(KEY_LIST_VIEW, staggered)
    notifyAdapterExtraChanged()
  }

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    setupData()
  }

  private fun loadNoteByStates(states: Array<String>) {
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<Note>> {
      override fun run(): List<Note> {
        val sorting = SortingOptionsBottomSheet.getSortingState(store)
        return sort(Note.db(this@MainActivity).getByNoteState(states), sorting)
      }

      override fun handle(notes: List<Note>) {
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
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<Note>> {
      override fun run(): List<Note> {
        val sorting = SortingOptionsBottomSheet.getSortingState(store)
        return sort(Note.db(this@MainActivity).getNoteByLocked(true), sorting)
      }

      override fun handle(notes: List<Note>) {
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

  private fun handleNewItems(notes: List<Note>) {
    adapter.clearItems()

    if (notes.isEmpty()) {
      adapter.addItem(EmptyRecyclerItem())
    }

    for (note in notes) {
      adapter.addItem(NoteRecyclerItem(note))
    }
  }

  fun moveItemToTrashOrDelete(note: Note) {
    if (mode === HomeNavigationState.TRASH) {
      note.delete(this)
      setupData()
      return
    }
    markItem(note, NoteState.TRASH)
  }

  fun openTag(tag: Tag) {
    mode = HomeNavigationState.TAG
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<Note>> {
      override fun run(): List<Note> {
        val listNoteWithTag = ArrayList<Note>()
        val notes = Note.db(this@MainActivity).all
        for (note in notes) {
          if (note.tagUUIDs.contains(tag.uuid)) {
            listNoteWithTag.add(note)
          }
        }

        val sorting = SortingOptionsBottomSheet.getSortingState(store)
        return sort(listNoteWithTag, sorting)
      }

      override fun handle(notes: List<Note>) {
        handleNewItems(notes)
      }
    })
    notifyModeChange()
  }

  fun updateNote(note: Note) {
    note.save(this)
    setupData()
  }

  fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
    setupData()
  }

  override fun onResume() {
    super.onResume()
    setupData()
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
        notes.add(NoteRecyclerItem(note))
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
  }

  override fun notifyThemeChange() {
    setSystemTheme()
    val theme = ThemeManager.get(this)

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = theme.get(this, ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)
    searchIcon.setColorFilter(toolbarIconColor)
    searchBackButton.setColorFilter(toolbarIconColor)
    searchCloseIcon.setColorFilter(toolbarIconColor)

    findViewById<View>(R.id.separator).setBackgroundColor(toolbarIconColor)

    val actionBarTitle = findViewById<TextView>(R.id.action_bar_title)
    actionBarTitle.setTextColor(theme.get(this, ThemeColorType.TERTIARY_TEXT))
    homeButton.setColorFilter(theme.get(this, ThemeColorType.ACCENT_TEXT))

    val textColor = theme.get(this, ThemeColorType.SECONDARY_TEXT)
    val textHintColor = theme.get(this, ThemeColorType.HINT_TEXT)
    searchBox.setTextColor(textColor)
    searchBox.setHintTextColor(textHintColor)
  }

  private fun registerNoteReceiver() {
    receiver = SyncedNoteBroadcastReceiver {
      setupData()
    }
    registerReceiver(receiver, getNoteIntentFilter())
  }
}
