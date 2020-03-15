package com.maubis.scarlet.base

import android.content.BroadcastReceiver
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.widget.GridLayout.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.auth.IPendingUploadListener
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.support.NoteExporter
import com.maubis.scarlet.base.export.support.PermissionUtils
import com.maubis.scarlet.base.main.*
import com.maubis.scarlet.base.main.HomeNavigationMode
import com.maubis.scarlet.base.main.SearchState
import com.maubis.scarlet.base.main.recycler.EmptyRecyclerItem
import com.maubis.scarlet.base.main.recycler.GenericRecyclerItem
import com.maubis.scarlet.base.main.recycler.getAppUpdateInformationItem
import com.maubis.scarlet.base.main.recycler.getBackupInformationItem
import com.maubis.scarlet.base.main.recycler.getInstallProInformationItem
import com.maubis.scarlet.base.main.recycler.getMigrateToProAppInformationItem
import com.maubis.scarlet.base.main.recycler.getReviewInformationItem
import com.maubis.scarlet.base.main.recycler.getSignInInformationItem
import com.maubis.scarlet.base.main.recycler.getThemeInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowAppUpdateInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowBackupInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowInstallProInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowMigrateToProAppInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowReviewInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowSignInformationItem
import com.maubis.scarlet.base.main.recycler.shouldShowThemeInformationItem
import com.maubis.scarlet.base.main.sheets.WhatsNewBottomSheet
import com.maubis.scarlet.base.main.sheets.openDeleteTrashSheet
import com.maubis.scarlet.base.main.specs.MainActivityBottomBar
import com.maubis.scarlet.base.main.specs.MainActivityDisabledSync
import com.maubis.scarlet.base.main.specs.MainActivityFolderBottomBar
import com.maubis.scarlet.base.main.specs.MainActivitySyncingNow
import com.maubis.scarlet.base.main.utils.MainSnackbar
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.folder.FolderRecyclerItem
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.softDelete
import com.maubis.scarlet.base.note.tag.view.TagsAndColorPickerViewHolder
import com.maubis.scarlet.base.service.SyncedNoteBroadcastReceiver
import com.maubis.scarlet.base.service.getNoteIntentFilter
import com.maubis.scarlet.base.settings.sheet.STORE_KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.maubis.scarlet.base.settings.sheet.sNoteItemLineCount
import com.maubis.scarlet.base.settings.sheet.sUIUseGridView
import com.maubis.scarlet.base.support.database.HouseKeeper
import com.maubis.scarlet.base.support.database.HouseKeeperJob
import com.maubis.scarlet.base.support.database.Migrator
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.ui.SecuredActivity
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.sThemeIsAutomatic
import com.maubis.scarlet.base.support.ui.setThemeFromSystem
import com.maubis.scarlet.base.support.utils.shouldShowWhatsNewSheet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_trash_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : SecuredActivity(), INoteOptionSheetActivity {
  companion object {
    private const val IS_IN_SEARCH_MODE: String = "IS_IN_SEARCH_MODE"
    private const val NAVIGATION_MODE: String = "NAVIGATION_MODE"
    private const val SEARCH_TEXT: String = "SEARCH_TEXT"
    private const val CURRENT_FOLDER_UUID: String = "CURRENT_FOLDER_UUID"
    private const val TAGS_UUIDS: String = "TAGS_UUIDS"
    private const val SEARCH_COLORS: String = "SEARCH_COLORS"
  }

  private val singleThreadDispatcher = newSingleThreadContext("singleThreadDispatcher")

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: NoteAppAdapter
  private lateinit var snackbar: MainSnackbar

  private lateinit var receiver: BroadcastReceiver
  private lateinit var tagAndColorPicker: TagsAndColorPickerViewHolder

  private var lastSyncPending: AtomicBoolean = AtomicBoolean(false)
  private var lastSyncHappening: AtomicBoolean = AtomicBoolean(false)

  val state: SearchState = SearchState(mode = HomeNavigationMode.DEFAULT)
  var isInSearchMode: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    handleIntent()

    // Migrate to the newer version of the tags
    Migrator(this).start()

    state.mode = HomeNavigationMode.DEFAULT

    setupRecyclerView()
    setListeners()

    if (sThemeIsAutomatic) {
      setThemeFromSystem(this)
    }
    sAppTheme.notifyChange(this)

    if (shouldShowWhatsNewSheet()) {
      openSheet(this, WhatsNewBottomSheet())
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putBoolean(IS_IN_SEARCH_MODE, isInSearchMode)
    outState.putString(SEARCH_TEXT, state.text)
    outState.putIntegerArrayList(SEARCH_COLORS, ArrayList(state.colors))
    outState.putInt(NAVIGATION_MODE, state.mode.ordinal)
    outState.putString(CURRENT_FOLDER_UUID, state.currentFolder?.uuid)
    outState.putStringArrayList(TAGS_UUIDS, ArrayList(state.tags.map { it.uuid }))
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)

    if (savedInstanceState != null) {
      isInSearchMode = savedInstanceState.getBoolean(IS_IN_SEARCH_MODE)
      state.text = savedInstanceState.getString(SEARCH_TEXT, "")
      state.colors = savedInstanceState.getIntegerArrayList(SEARCH_COLORS) ?: ArrayList()
      state.mode = HomeNavigationMode.values()[savedInstanceState.getInt(NAVIGATION_MODE)]
      savedInstanceState.getString(CURRENT_FOLDER_UUID)?.let {
        state.currentFolder = instance.foldersDatabase().getByUUID(it)
      }
      savedInstanceState.getStringArrayList(TAGS_UUIDS)?.forEach {
        instance.tagsDatabase().getByUUID(it)?.let { state.tags.add(it) }
      }
    }
  }

  override fun onConfigurationChanged(configuration: Configuration) {
    super.onConfigurationChanged(configuration)
    startActivity(MainActivityActions.NIL.intent(this))
    finish()
  }

  fun setListeners() {
    snackbar = MainSnackbar(bottomSnackbar) { loadData() }
    searchBackButton.setOnClickListener {
      onBackPressed()
    }
    searchCloseIcon.setOnClickListener { onBackPressed() }
    searchBox.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        startSearch(charSequence.toString())
      }

      override fun afterTextChanged(editable: Editable) {}
    })
    tagAndColorPicker = TagsAndColorPickerViewHolder(
      this,
      tagsFlexBox,
      { tag ->
        val isTagSelected = state.tags.filter { it.uuid == tag.uuid }.isNotEmpty()
        when (isTagSelected) {
          true -> {
            state.tags.removeAll { it.uuid == tag.uuid }
            startSearch(searchBox.text.toString())
            tagAndColorPicker.notifyChanged()
          }
          false -> {
            openTag(tag)
            tagAndColorPicker.notifyChanged()
          }
        }
      },
      { color ->
        when (state.colors.contains(color)) {
          true -> state.colors.remove(color)
          false -> state.colors.add(color)
        }
        tagAndColorPicker.notifyChanged()
        startSearch(searchBox.text.toString())
      })
  }

  fun setupRecyclerView() {
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = sAppPreferences.get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = sAppPreferences.get(KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)

    adapter = NoteAppAdapter(this, sUIUseGridView, isTablet)
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
      .setView(this, R.id.recycler_view)
      .setAdapter(adapter)
      .setLayoutManager(getLayoutManager(sUIUseGridView, isTablet))
      .build()

    vSwipeToRefresh.setOnRefreshListener {
      when {
        instance.authenticator().isLoggedIn(this)
          && !instance.authenticator().isLegacyLoggedIn()
          && !lastSyncHappening.get() -> instance.authenticator().requestSync(true)
        else -> vSwipeToRefresh.isRefreshing = false
      }
    }
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    return when {
      isTabletView || isStaggeredView -> StaggeredGridLayoutManager(2, VERTICAL)
      else -> LinearLayoutManager(this)
    }
  }

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    resetAndLoadData()
  }

  fun onModeChange(mode: HomeNavigationMode) {
    GlobalScope.launch(Dispatchers.Main) {
      state.mode = mode
      unifiedSearch()
      notifyModeChange()
    }
  }

  private fun notifyModeChange() {
    val isTrash = state.mode === HomeNavigationMode.TRASH
    trashNoticeToolbar.visibility = if (isTrash) View.VISIBLE else GONE
    setBottomToolbar()
  }

  fun onFolderChange(folder: Folder?) {
    GlobalScope.launch(Dispatchers.Main) {
      state.currentFolder = folder
      unifiedSearch()
      notifyFolderChange()
    }
  }

  private fun notifyFolderChange() {
    val componentContext = ComponentContext(this)
    lithoPreBottomToolbar.removeAllViews()
    setBottomToolbar()

    val currentFolder = state.currentFolder
    if (currentFolder != null) {
      lithoPreBottomToolbar.addView(LithoView.create(componentContext,
          MainActivityFolderBottomBar.create(componentContext)
              .folder(currentFolder)
              .build()))
    }
    else
      notifyDisabledLegacySync()
  }

  private fun handleNewItems(notes: List<RecyclerItem>) {
    adapter.clearItems()
    if (!isInSearchMode) {
      adapter.addItem(GenericRecyclerItem(RecyclerItem.Type.TOOLBAR))
      addInformationItem(1)
    }
    if (notes.isEmpty()) {
      adapter.addItem(EmptyRecyclerItem())
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
  }

  private fun addInformationItem(index: Int) {
    val informationItem = when {
      shouldShowMigrateToProAppInformationItem(this) -> getMigrateToProAppInformationItem(this)
      shouldShowSignInformationItem(this) -> getSignInInformationItem(this)
      shouldShowAppUpdateInformationItem() -> getAppUpdateInformationItem(this)
      shouldShowReviewInformationItem() -> getReviewInformationItem(this)
      shouldShowInstallProInformationItem() -> getInstallProInformationItem(this)
      shouldShowThemeInformationItem() -> getThemeInformationItem(this)
      shouldShowBackupInformationItem() -> getBackupInformationItem(this)
      else -> null
    }
    if (informationItem === null) {
      return
    }
    adapter.addItem(informationItem, index)
  }

  private suspend fun unifiedSearchSynchronous(): List<RecyclerItem> {
    val allItems = emptyList<RecyclerItem>().toMutableList()
    if (state.currentFolder != null) {
      val allNotes = unifiedSearchSynchronous(state)
      allItems.addAll(allNotes
                        .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
                        .map { it.await() })
      return allItems
    }

    val allNotes = unifiedSearchWithoutFolder(state)
    val directAcceptableFolders = filterDirectlyValidFolders(state)
    allItems.addAll(CoreConfig.foldersDb.getAll()
                      .map {
                        GlobalScope.async(Dispatchers.IO) {
                          val isDirectFolder = directAcceptableFolders.contains(it)
                          val notesCount = filterFolder(allNotes, it).size
                          if (state.hasFilter() && notesCount == 0 && !isDirectFolder) {
                            return@async null
                          }

                          FolderRecyclerItem(
                            context = this@MainActivity,
                            folder = it,
                            click = { onFolderChange(it) },
                            longClick = {
                              CreateOrEditFolderBottomSheet.openSheet(this@MainActivity, it, { _, _ -> loadData() })
                            },
                            selected = state.currentFolder?.uuid == it.uuid,
                            contents = notesCount)
                        }
                      }
                      .map { it.await() }
                      .filterNotNull())
    allItems.addAll(filterOutFolders(allNotes)
                      .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
                      .map { it.await() })
    return allItems
  }

  private fun notifyDisabledLegacySync() {
    val componentContext = ComponentContext(this)
    lithoPreBottomToolbar.removeAllViews()
    if (!instance.authenticator().isLegacyLoggedIn()) {
      return
    }

    lithoPreBottomToolbar.addView(LithoView.create(componentContext,
                                                   MainActivityDisabledSync.create(componentContext)
                                                     .onClick {
                                                       instance.authenticator().openTransferDataActivity(componentContext.androidContext)?.run()
                                                     }
                                                     .build()))
  }

  fun notifySyncingInformation(isSyncHappening: Boolean, isSyncPending: Boolean) {
    val componentContext = ComponentContext(this)
    if (!instance.authenticator().isLoggedIn(this)
      || instance.authenticator().isLegacyLoggedIn()) {
      return
    }

    if (lastSyncPending.getAndSet(isSyncPending) == isSyncPending
      && lastSyncHappening.getAndSet(isSyncHappening) == isSyncHappening) {
      return
    }

    if (!isSyncPending && !isSyncHappening) {
      GlobalScope.launch(Dispatchers.Main) {
        lithoSyncingBottomToolbar.removeAllViews()
      }
      return
    }

    GlobalScope.launch(Dispatchers.Main) {
      lithoSyncingBottomToolbar.removeAllViews()
      lithoSyncingBottomToolbar.addView(LithoView.create(componentContext,
                                                         MainActivitySyncingNow.create(componentContext)
                                                           .isSyncHappening(isSyncHappening)
                                                           .onClick {
                                                             if (!lastSyncHappening.get()) {
                                                               instance.authenticator().requestSync(true)
                                                             }
                                                           }
                                                           .onLongClick {
                                                             if (!lastSyncHappening.get()) {
                                                               instance.authenticator().showPendingSync(this@MainActivity)
                                                             }
                                                           }
                                                           .build()))
      if (!isSyncHappening && isSyncPending) {
        instance.authenticator().requestSync(false)
      }
    }
  }

  private fun unifiedSearch() {
    GlobalScope.launch(Dispatchers.Main) {
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      handleNewItems(items.await())
    }
  }

  fun openTag(tag: Tag) {
    state.mode = if (state.mode == HomeNavigationMode.LOCKED) HomeNavigationMode.DEFAULT else state.mode
    state.tags.add(tag)
    unifiedSearch()
    notifyModeChange()
  }

  override fun onResume() {
    super.onResume()
    instance.startListener(this)
    loadData()
    registerNoteReceiver()
    notifyFolderChange()

    if (isInSearchMode)
      enterSearchMode()

    instance.authenticator().setPendingUploadListener(object : IPendingUploadListener {
      override fun onPendingSyncsUpdate(isSyncHappening: Boolean) {
        notifySyncingInformation(isSyncHappening, lastSyncPending.get())
        GlobalScope.launch(Dispatchers.Main) {
          vSwipeToRefresh.isRefreshing = false
        }
      }

      override fun onPendingStateUpdate(isDataSyncPending: Boolean) {
        notifySyncingInformation(lastSyncHappening.get(), isDataSyncPending)
      }
    })
    instance.authenticator().requestSync(false)
  }

  fun resetAndLoadData() {
    state.clear()
    loadData()
  }

  fun loadData() = onModeChange(state.mode)

  fun enterSearchMode() {
    isInSearchMode = true
    searchBox.setText(state.text)
    searchToolbar.visibility = View.VISIBLE
    tryOpeningTheKeyboard()
    GlobalScope.launch(Dispatchers.Main) {
      GlobalScope.async(Dispatchers.IO) { tagAndColorPicker.reset() }.await()
      tagAndColorPicker.notifyChanged()
    }
    searchBox.requestFocus()
  }

  fun quitSearchMode() {
    isInSearchMode = false
    searchBox.setText("")
    tryClosingTheKeyboard()
    searchToolbar.visibility = View.GONE
    state.clearSearchBar()
    loadData()
  }

  private fun startSearch(keyword: String) {
    GlobalScope.launch(singleThreadDispatcher) {
      state.text = keyword
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      GlobalScope.launch(Dispatchers.Main) {
        handleNewItems(items.await())
      }
    }
  }

  override fun onBackPressed() {
    when {
      isInSearchMode && searchBox.text.toString().isBlank() -> quitSearchMode()
      isInSearchMode -> searchBox.setText("")
      state.currentFolder != null -> onFolderChange(null)
      state.hasFilter() -> {
        state.clear()
        onModeChange(HomeNavigationMode.DEFAULT)
        notifyFolderChange()
      }
      else -> super.onBackPressed()
    }
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(receiver)
    instance.authenticator().setPendingUploadListener(null)
  }

  override fun onDestroy() {
    super.onDestroy()
    HouseKeeperJob.schedule()
  }

  override fun onStop() {
    super.onStop()
    if (PermissionUtils().getStoragePermissionManager(this).hasAllPermissions()) {
      HouseKeeper(this).removeOlderClips()
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()
    containerLayoutMain.setBackgroundColor(getThemeColor())

    val toolbarIconColor = sAppTheme.get(ThemeColorType.TOOLBAR_ICON)
    deletesAutomatically.setTextColor(toolbarIconColor)

    setBottomToolbar()
  }

  private fun registerNoteReceiver() {
    receiver = SyncedNoteBroadcastReceiver {
      loadData()
    }
    registerReceiver(receiver, getNoteIntentFilter())
  }

  private fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.removeAllViews()
    lithoBottomToolbar.addView(
      LithoView.create(
        componentContext,
        MainActivityBottomBar.create(componentContext)
          .colorConfig(ToolbarColorConfig())
          .disableNewFolderButton(state.currentFolder != null)
          .isInTrash(state.mode == HomeNavigationMode.TRASH)
          .build()))
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */
  override fun updateNote(note: Note) {
    note.save(this)
    loadData()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
    loadData()
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    snackbar.softUndo(this, note)
    note.softDelete(this)
    loadData()
  }

  override fun notifyTagsChanged(note: Note) {
    loadData()
  }

  override fun getSelectMode(note: Note): String {
    return state.mode.name
  }

  override fun notifyResetOrDismiss() {
    loadData()
  }

  override fun lockedContentIsHidden() = true

  /**
   * End : INoteOptionSheetActivity
   */
}
