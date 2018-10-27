package com.maubis.scarlet.base

import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import android.widget.LinearLayout
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.folder.FolderBuilder
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
import com.maubis.scarlet.base.main.utils.MainSnackbar
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
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
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.sheet.NoteSettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.SearchConfig
import com.maubis.scarlet.base.support.bind
import com.maubis.scarlet.base.support.database.HouseKeeper
import com.maubis.scarlet.base.support.database.Migrator
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.unifiedFolderSearchSynchronous
import com.maubis.scarlet.base.support.unifiedSearchSynchronous

class MainActivity : ThemedActivity(), ITutorialActivity, INoteOptionSheetActivity {

  internal lateinit var recyclerView: RecyclerView
  internal lateinit var adapter: NoteAppAdapter
  internal lateinit var snackbar: MainSnackbar

  internal lateinit var receiver: BroadcastReceiver
  internal lateinit var executor: SimpleThreadExecutor
  internal lateinit var tagAndColorPicker: TagsAndColorPickerViewHolder

  var config: SearchConfig = SearchConfig(mode = HomeNavigationState.DEFAULT)
  var isInSearchMode: Boolean = false

  val searchBackButton: ImageView by bind(R.id.search_back_button)
  val searchCloseIcon: ImageView by bind(R.id.search_close_button)
  val deleteTrashIcon: ImageView by bind(R.id.menu_delete_everything)
  val deletesAutomatically: TextView by bind(R.id.deletes_automatically)
  val searchBox: EditText by bind(R.id.search_box)
  val toolbarMenu: ImageView by bind(R.id.toolbar_icon_options)
  val toolbarIconNewFolder: ImageView by bind(R.id.toolbar_icon_new_folder)
  val toolbarIconNewChecklist: ImageView by bind(R.id.toolbar_icon_new_checklist)
  val toolbarIconNewNote: ImageView by bind(R.id.toolbar_icon_new_note)
  val searchToolbar: View by bind(R.id.search_toolbar)
  val tagsFlexBox: FlexboxLayout by bind(R.id.tags_flexbox)
  val deleteToolbar: View by bind(R.id.bottom_delete_toolbar_layout)
  val bottomSnackbar: LinearLayout by bind(R.id.bottom_snackbar)
  val bottomToolbar: LinearLayout by bind(R.id.toolbar_bottom)
  val folderToolbar: LinearLayout by bind(R.id.folder_toolbar)
  val folderIconClose: ImageView by bind(R.id.folder_toolbar_close)
  val folderName: TextView by bind(R.id.folder_toolbar_name)
  val folderIconOptions: ImageView by bind(R.id.folder_toolbar_options)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Migrate to the newer version of the tags
    Migrator(this).start()

    config.mode = HomeNavigationState.DEFAULT
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
    snackbar = MainSnackbar(bottomSnackbar, { setupData() })
    deleteTrashIcon.setOnClickListener { AlertBottomSheet.openDeleteTrashSheet(this@MainActivity) }
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
    toolbarMenu.setOnClickListener { HomeNavigationBottomSheet.openSheet(this@MainActivity) }
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
    toolbarIconNewFolder.setOnClickListener {
      CreateOrEditFolderBottomSheet.openSheet(
          this,
          FolderBuilder().emptyFolder(NoteSettingsOptionsBottomSheet.genDefaultColor()),
          { _, _ -> setupData() })
    }
    toolbarIconNewChecklist.setOnClickListener {
      val intent = CreateNoteActivity.getNewChecklistNoteIntent(
          this@MainActivity,
          config.folders.firstOrNull()?.uuid ?: "")
      this@MainActivity.startActivity(intent)
    }
    toolbarIconNewNote.setOnClickListener {
      val intent = CreateNoteActivity.getNewNoteIntent(
          this@MainActivity,
          config.folders.firstOrNull()?.uuid ?: "")
      this@MainActivity.startActivity(intent)
    }
  }

  fun setupRecyclerView() {
    val staggeredView = UISettingsOptionsBottomSheet.useGridView
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
    config.resetMode(HomeNavigationState.DEFAULT)
    unifiedSearch()
    notifyModeChange()
  }

  fun onFavouritesClick() {
    config.resetMode(HomeNavigationState.FAVOURITE)
    unifiedSearch()
    notifyModeChange()
  }

  fun onArchivedClick() {
    config.resetMode(HomeNavigationState.ARCHIVED)
    unifiedSearch()
    notifyModeChange()
  }

  fun onTrashClick() {
    config.resetMode(HomeNavigationState.TRASH)
    unifiedSearch()
    notifyModeChange()
  }

  fun onLockedClick() {
    config.resetMode(HomeNavigationState.LOCKED)
    MultiAsyncTask.execute(object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
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
      shouldShowSignInformationItem() -> getSignInInformationItem(this)
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

  private fun unifiedSearchSynchronous(): List<RecyclerItem> {
    val allItems = emptyList<RecyclerItem>().toMutableList()
    allItems.addAll(unifiedFolderSearchSynchronous(config)
        .map {
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
              selected = config.hasFolder(it))
        })
    allItems.addAll(unifiedSearchSynchronous(config)
        .map { NoteRecyclerItem(this@MainActivity, it) })
    return allItems
  }

  private fun notifyFolderChange() {
    if (config.folders.isEmpty()) {
      folderToolbar.visibility = View.GONE
      return
    }
    val folder = config.folders.first()
    folderToolbar.visibility = View.VISIBLE
    folderToolbar.setBackgroundColor(folder.color)
    folderIconClose.setOnClickListener {
      config.folders.clear()
      unifiedSearch()
      notifyFolderChange()
    }
    folderIconOptions.setOnClickListener {
      if (config.folders.isEmpty()) {
        return@setOnClickListener
      }
      CreateOrEditFolderBottomSheet.openSheet(this@MainActivity, folder, { _, _ -> setupData() })
    }
    folderName.setText(folder.title)

    val isLightShaded = ColorUtil.isLightColored(folder.color)
    val color = when (isLightShaded) {
      true -> ContextCompat.getColor(this, R.color.dark_tertiary_text)
      false -> ContextCompat.getColor(this, R.color.light_secondary_text)
    }
    folderName.setTextColor(color)
    folderIconClose.setColorFilter(color)
    folderIconOptions.setColorFilter(color)
  }

  private fun unifiedSearch() {
    MultiAsyncTask.execute(object : MultiAsyncTask.Task<List<RecyclerItem>> {
      override fun run(): List<RecyclerItem> {
        return unifiedSearchSynchronous()
      }

      override fun handle(notes: List<RecyclerItem>) {
        handleNewItems(notes)
      }
    })
  }

  fun openTag(tag: Tag) {
    config.mode = if (config.mode == HomeNavigationState.LOCKED) HomeNavigationState.DEFAULT else config.mode
    config.tags.add(tag)
    unifiedSearch()
    notifyModeChange()
  }

  override fun onResume() {
    super.onResume()
    CoreConfig.instance.startListener(this)
    setupData()
    registerNoteReceiver()
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
      MultiAsyncTask.execute(object : MultiAsyncTask.Task<Unit> {
        override fun run() {
          tagAndColorPicker.reset()
        }

        override fun handle(result: Unit) {
          tagAndColorPicker.notifyChanged()
        }
      })
      searchBox.requestFocus()
    } else {
      tryClosingTheKeyboard()
      resetAndSetupData()
    }
  }

  private fun startSearch(keyword: String) {
    executor.executeNow {
      config.text = keyword
      val items = unifiedSearchSynchronous()
      runOnUiThread {
        handleNewItems(items)
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
      }
      else -> super.onBackPressed()
    }
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(receiver)
    HouseKeeper(this.applicationContext).start()
  }

  override fun onStop() {
    super.onStop()
    if (PermissionUtils().getStoragePermissionManager(this).hasAllPermissions()) {
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()

    val theme = CoreConfig.instance.themeController()
    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)

    toolbarMenu.setColorFilter(toolbarIconColor)
    toolbarIconNewFolder.setColorFilter(toolbarIconColor)
    toolbarIconNewNote.setColorFilter(toolbarIconColor)
    toolbarIconNewChecklist.setColorFilter(toolbarIconColor)

    bottomToolbar.setBackgroundColor(theme.get(ThemeColorType.TOOLBAR_BACKGROUND))
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
      TUTORIAL_KEY_NEW_NOTE -> createHint(this, toolbarIconNewNote,
          getString(R.string.tutorial_create_a_new_note),
          getString(R.string.main_no_notes_hint))
      TUTORIAL_KEY_HOME_SETTINGS -> createHint(this, toolbarMenu,
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
