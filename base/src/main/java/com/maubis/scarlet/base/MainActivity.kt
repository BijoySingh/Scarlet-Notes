package com.maubis.scarlet.base

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
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.github.bijoysingh.starter.util.IntentUtils
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.export.support.NoteExporter
import com.maubis.scarlet.base.export.support.PermissionUtils
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.main.activity.ITutorialActivity
import com.maubis.scarlet.base.main.activity.createHint
import com.maubis.scarlet.base.main.recycler.*
import com.maubis.scarlet.base.main.sheets.AlertBottomSheet
import com.maubis.scarlet.base.main.sheets.HomeNavigationBottomSheet
import com.maubis.scarlet.base.main.sheets.WhatsNewItemsBottomSheet
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.search
import com.maubis.scarlet.base.note.softDelete
import com.maubis.scarlet.base.note.tag.view.TagPickerViewHolder
import com.maubis.scarlet.base.service.SyncedNoteBroadcastReceiver
import com.maubis.scarlet.base.service.getNoteIntentFilter
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.Flavor
import com.maubis.scarlet.base.support.bind
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.database.tagsDB
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.utils.migrate
import com.maubis.scarlet.base.utils.removeOlderClips

class MainActivity : ThemedActivity(), ITutorialActivity, INoteOptionSheetActivity {

  internal lateinit var recyclerView: RecyclerView
  internal lateinit var adapter: NoteAppAdapter

  internal var mode: HomeNavigationState = HomeNavigationState.DEFAULT
  internal var selectedTag: Tag? = null

  internal lateinit var receiver: BroadcastReceiver
  internal lateinit var executor: SimpleThreadExecutor
  internal lateinit var tagPicker: TagPickerViewHolder

  val homeButton: ImageView by bind(R.id.home_button)
  val searchIcon: ImageView by bind(R.id.home_search_button)
  val searchBackButton: ImageView by bind(R.id.search_back_button)
  val searchCloseIcon: ImageView by bind(R.id.search_close_button)
  val deleteTrashIcon: ImageView by bind(R.id.menu_delete_everything)
  val deletesAutomatically: TextView by bind(R.id.deletes_automatically)
  val searchBox: EditText by bind(R.id.search_box)
  val mainToolbar: View by bind(R.id.main_toolbar)
  val mainToolbarTitle: TextView by bind(R.id.action_bar_title)
  val searchToolbar: View by bind(R.id.search_toolbar)
  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)
  val tagsFlexBox: FlexboxLayout by bind(R.id.tags_flexbox)

  val deleteToolbar: View by bind(R.id.bottom_delete_toolbar_layout)
  internal var isInSearchMode: Boolean = false

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
        startSearch(charSequence.toString())
      }

      override fun afterTextChanged(editable: Editable) {

      }
    })
    homeButton.setOnClickListener { HomeNavigationBottomSheet.openSheet(this@MainActivity) }
    primaryFab.setOnClickListener { IntentUtils.startActivity(this@MainActivity, CreateNoteActivity::class.java) }
    secondaryFab.setOnClickListener { HomeNavigationBottomSheet.openSheet(this@MainActivity) }
    tagPicker = TagPickerViewHolder(this, tagsFlexBox, {
      if (it == selectedTag && mode == HomeNavigationState.TAG) {
        mode = HomeNavigationState.DEFAULT
        startSearch(searchBox.text.toString())
        return@TagPickerViewHolder
      }
      openTag(it)
      tagPicker.setTags(listOf(it))
    })
  }

  fun setupRecyclerView() {
    val staggeredView = UISettingsOptionsBottomSheet.isGridView()
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = CoreConfig.instance.store().get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = CoreConfig.instance.store().get(KEY_MARKDOWN_HOME_ENABLED, true)
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
        return sort(notesDB.getByNoteState(states), sorting)
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
        return sort(notesDB.getNoteByLocked(true), sorting)
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
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
    addInformationItem(1)
  }

  private fun addInformationItem(index: Int) {
    val informationItem = when {
      !CoreConfig.instance.remoteConfigFetcher().isLatestVersion() -> getAppUpdateInformationItem(this)
      probability(0.01f)
          && !CoreConfig.instance.store().get(KEY_INFO_RATE_AND_REVIEW, false) -> getReviewInformationItem(this)
      probability(0.01f)
          && !CoreConfig.instance.store().get(KEY_INFO_INSTALL_PRO, false)
          && CoreConfig.instance.appFlavor() != Flavor.PRO -> getInstallProInformationItem(this)
      else -> null
    }
    if (informationItem === null) {
      return
    }
    adapter.addItem(informationItem, index)
  }

  fun openTag(tag: Tag) {
    mode = HomeNavigationState.TAG
    selectedTag = tag
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
      override fun run(): List<NoteRecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        return sort(notesDB.getNoteByTag(tag.uuid), sorting)
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
    CoreConfig.instance.startListener(this)
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

  fun getDataForMode(): List<Note> {
    return when (mode) {
      HomeNavigationState.FAVOURITE -> notesDB.getByNoteState(arrayOf(NoteState.FAVOURITE.name))
      HomeNavigationState.ARCHIVED -> notesDB.getByNoteState(arrayOf(NoteState.ARCHIVED.name))
      HomeNavigationState.TRASH -> notesDB.getByNoteState(arrayOf(NoteState.TRASH.name))
      HomeNavigationState.LOCKED -> notesDB.getNoteByLocked(true)
      HomeNavigationState.DEFAULT -> notesDB.getByNoteState(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
      HomeNavigationState.TAG -> notesDB.getNoteByTag(selectedTag!!.uuid)
    }
  }

  private fun setSearchMode(mode: Boolean) {
    isInSearchMode = mode
    mainToolbar.visibility = if (isInSearchMode) View.GONE else View.VISIBLE
    searchToolbar.visibility = if (isInSearchMode) View.VISIBLE else View.GONE
    searchBox.setText("")

    if (isInSearchMode) {
      tryOpeningTheKeyboard()
      tagPicker.search("")
    } else {
      tryClosingTheKeyboard()
      setupData()
    }
  }

  private fun startSearch(keyword: String) {
    executor.executeNow {
      val items = search(keyword)
      val tags = if (mode != HomeNavigationState.TAG) tagsDB.search(keyword)
      else listOf(selectedTag!!)

      runOnUiThread {
        adapter.items = items
        tagPicker.setTags(tags)
      }
    }
  }

  private fun search(keyword: String): List<RecyclerItem> {
    return getDataForMode()
        .filter { return@filter it.search(keyword) }
        .map { NoteRecyclerItem(this, it) }
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
    if (PermissionUtils().getStoragePermissionManager(this).hasAllPermissions()) {
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)
    mainToolbarTitle.text = getString(R.string.search_toolbar_text, getString(R.string.app_name))
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
      notesDB.getCount() == 0 -> showHint(TUTORIAL_KEY_NEW_NOTE)
      shouldShowHint(TUTORIAL_KEY_NEW_NOTE) -> showHint(TUTORIAL_KEY_NEW_NOTE)
      shouldShowHint(TUTORIAL_KEY_HOME_SETTINGS) -> showHint(TUTORIAL_KEY_HOME_SETTINGS)
      else -> return false
    }

    return true
  }

  override fun shouldShowHint(key: String): Boolean {
    return !CoreConfig.instance.store().get(key, false)
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
    CoreConfig.instance.store().put(key, true)
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
    note.softDelete(this)
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
