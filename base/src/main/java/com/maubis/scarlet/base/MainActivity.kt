package com.maubis.scarlet.base

import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.widget.GridLayout.VERTICAL
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.auth.IPendingUploadListener
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.support.NoteExporter
import com.maubis.scarlet.base.export.support.PermissionUtils
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.main.recycler.*
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
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.sNoteItemLineCount
import com.maubis.scarlet.base.support.SearchConfig
import com.maubis.scarlet.base.support.database.HouseKeeperJob
import com.maubis.scarlet.base.support.database.Migrator
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.unifiedFolderSearchSynchronous
import com.maubis.scarlet.base.support.unifiedSearchSynchronous
import com.maubis.scarlet.base.support.utils.shouldShowWhatsNewSheet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_trash_info.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ThemedActivity(), INoteOptionSheetActivity {
  private val singleThreadDispatcher = newSingleThreadContext("singleThreadDispatcher")

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: NoteAppAdapter
  private lateinit var snackbar: MainSnackbar

  private lateinit var receiver: BroadcastReceiver
  private lateinit var tagAndColorPicker: TagsAndColorPickerViewHolder

  private var lastSyncPending: AtomicBoolean = AtomicBoolean(false)
  private var lastSyncHappening: AtomicBoolean = AtomicBoolean(false)

  var config: SearchConfig = SearchConfig(mode = HomeNavigationState.DEFAULT)
  var isInSearchMode: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Migrate to the newer version of the tags
    Migrator(this).start()

    config.mode = HomeNavigationState.DEFAULT

    setupRecyclerView()
    setListeners()
    notifyThemeChange()

    if (shouldShowWhatsNewSheet()) {
      openSheet(this, WhatsNewBottomSheet())
    }
  }

  fun setListeners() {
    snackbar = MainSnackbar(bottomSnackbar, { setupData() })
    deleteTrashIcon.setOnClickListener { openDeleteTrashSheet(this@MainActivity) }
    searchBackButton.setOnClickListener {
      onBackPressed()
    }
    searchCloseIcon.setOnClickListener { onBackPressed() }
    searchBox.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

      }

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        startSearch(charSequence.toString())
      }

      override fun afterTextChanged(editable: Editable) {

      }
    })
    tagAndColorPicker = TagsAndColorPickerViewHolder(
        this,
        tagsFlexBox,
        { tag ->
          val isTagSelected = config.tags.filter { it.uuid == tag.uuid }.isNotEmpty()
          when (isTagSelected) {
            true -> {
              config.tags.removeAll { it.uuid == tag.uuid }
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
          when (config.colors.contains(color)) {
            true -> config.colors.remove(color)
            false -> config.colors.add(color)
          }
          tagAndColorPicker.notifyChanged()
          startSearch(searchBox.text.toString())
        })
  }

  fun setupRecyclerView() {
    val staggeredView = UISettingsOptionsBottomSheet.useGridView
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = ApplicationBase.instance.store().get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = ApplicationBase.instance.store().get(KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)

    adapter = NoteAppAdapter(this, staggeredView, isTablet)
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
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
    resetAndSetupData()
  }

  /**
   * Start: Home Navigation Clicks
   */
  fun onHomeClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.DEFAULT)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onFavouritesClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.FAVOURITE)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onArchivedClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.ARCHIVED)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onTrashClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.TRASH)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onLockedClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.LOCKED)
      unifiedSearch()
      notifyModeChange()
    }
  }

  private fun notifyModeChange() {
    val isTrash = config.mode === HomeNavigationState.TRASH
    deleteToolbar.visibility = if (isTrash) View.VISIBLE else GONE
  }

  /**
   * End: Home Navigation Clicks
   */

  private fun handleNewItems(notes: List<RecyclerItem>) {
    adapter.clearItems()
    if (!isInSearchMode) {
      adapter.addItem(GenericRecyclerItem(RecyclerItem.Type.TOOLBAR))
    }
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
    allItems.addAll(unifiedFolderSearchSynchronous(config)
        .map {
          GlobalScope.async(Dispatchers.IO) {
            var notesCount = -1
            if (config.hasFilter()) {
              val folderConfig = config.copy()
              folderConfig.folders.clear()
              folderConfig.folders.add(it)
              notesCount = unifiedSearchSynchronous(folderConfig).size
              if (notesCount == 0) {
                return@async null
              }
              folderConfig.folders.clear()
            }
            FolderRecyclerItem(
                context = this@MainActivity,
                folder = it,
                click = {
                  config.folders.clear()
                  config.folders.add(it)
                  unifiedSearch()
                  notifyFolderChange()
                },
                longClick = {
                  CreateOrEditFolderBottomSheet.openSheet(this@MainActivity, it, { _, _ -> setupData() })
                },
                selected = config.hasFolder(it),
                contents = notesCount)
          }
        }
        .map { it.await() }
        .filterNotNull())
    allItems.addAll(unifiedSearchSynchronous(config)
        .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
        .map { it.await() })
    return allItems
  }

  fun notifyFolderChange() {
    val componentContext = ComponentContext(this)
    lithoPreBottomToolbar.removeAllViews()
    if (config.folders.isEmpty()) {
      return
    }

    val folder = config.folders.first()
    lithoPreBottomToolbar.addView(LithoView.create(componentContext,
        MainActivityFolderBottomBar.create(componentContext)
            .folder(folder)
            .build()))
  }

  fun notifyDisabledSync() {
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
              .build()))
      if (!isSyncHappening && isSyncPending) {
        instance.authenticator().requestSync(false)
      }
    }
  }

  fun unifiedSearch() {
    GlobalScope.launch(Dispatchers.Main) {
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      handleNewItems(items.await())
    }
  }

  fun openTag(tag: Tag) {
    config.mode = if (config.mode == HomeNavigationState.LOCKED) HomeNavigationState.DEFAULT else config.mode
    config.tags.add(tag)
    unifiedSearch()
    notifyModeChange()
  }

  override fun onResume() {
    super.onResume()
    ApplicationBase.instance.startListener(this)
    setupData()
    registerNoteReceiver()

    notifyDisabledSync()
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

  fun resetAndSetupData() {
    config.clear()
    setupData()
  }

  fun setupData() {
    return when (config.mode) {
      HomeNavigationState.FAVOURITE -> onFavouritesClick()
      HomeNavigationState.ARCHIVED -> onArchivedClick()
      HomeNavigationState.TRASH -> onTrashClick()
      HomeNavigationState.LOCKED -> onLockedClick()
      HomeNavigationState.DEFAULT -> onHomeClick()
      else -> onHomeClick()
    }
  }

  fun setSearchMode(mode: Boolean) {
    isInSearchMode = mode
    searchToolbar.visibility = if (isInSearchMode) View.VISIBLE else View.GONE
    searchBox.setText("")

    if (isInSearchMode) {
      tryOpeningTheKeyboard()
      GlobalScope.launch(Dispatchers.Main) {
        GlobalScope.async(Dispatchers.IO) { tagAndColorPicker.reset() }.await()
        tagAndColorPicker.notifyChanged()
      }
      searchBox.requestFocus()
    } else {
      tryClosingTheKeyboard()
      config.clearSearchBar()
      setupData()
    }
  }

  private fun startSearch(keyword: String) {
    GlobalScope.launch(singleThreadDispatcher) {
      config.text = keyword
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      GlobalScope.launch(Dispatchers.Main) {
        handleNewItems(items.await())
      }
    }
  }

  override fun onBackPressed() {
    when {
      isInSearchMode && searchBox.text.toString().isBlank() -> setSearchMode(false)
      isInSearchMode -> searchBox.setText("")
      config.hasFilter() -> {
        config.clear()
        onHomeClick()
        notifyFolderChange()
        notifyDisabledSync()
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
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()

    val theme = ApplicationBase.instance.themeController()
    containerLayoutMain.setBackgroundColor(getThemeColor())

    val toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)

    setBottomToolbar()
  }

  private fun registerNoteReceiver() {
    receiver = SyncedNoteBroadcastReceiver {
      setupData()
    }
    registerReceiver(receiver, getNoteIntentFilter())
  }

  fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.removeAllViews()
    lithoBottomToolbar.addView(LithoView.create(componentContext,
        MainActivityBottomBar.create(componentContext)
            .colorConfig(ToolbarColorConfig())
            .build()))
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
    snackbar.softUndo(this, note)
    note.softDelete(this)
    setupData()
  }

  override fun notifyTagsChanged(note: Note) {
    setupData()
  }

  override fun getSelectMode(note: Note): String {
    return config.mode.name
  }

  override fun notifyResetOrDismiss() {
    setupData()
  }

  override fun lockedContentIsHidden() = true

  /**
   * End : INoteOptionSheetActivity
   */
}
