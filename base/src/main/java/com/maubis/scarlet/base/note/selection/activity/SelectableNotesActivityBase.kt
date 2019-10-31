package com.maubis.scarlet.base.note.selection.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.main.recycler.EmptyRecyclerItem
import com.maubis.scarlet.base.note.folder.SelectorFolderRecyclerItem
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.recycler.getSelectableRecyclerItemControllerList
import com.maubis.scarlet.base.settings.sheet.STORE_KEY_LINE_COUNT
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.sNoteItemLineCount
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.SecuredActivity
import com.maubis.scarlet.base.support.ui.ThemeColorType

abstract class SelectableNotesActivityBase : SecuredActivity(), INoteSelectorActivity {

  lateinit var recyclerView: RecyclerView
  lateinit var adapter: NoteAppAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayoutUI())
  }

  open fun initUI() {
    notifyThemeChange()
    setupRecyclerView()

    MultiAsyncTask.execute(object : MultiAsyncTask.Task<List<RecyclerItem>> {
      override fun run(): List<RecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        val notes = sort(getNotes(), sorting)
          .sortedBy { it.folder }
          .map { NoteRecyclerItem(this@SelectableNotesActivityBase, it) }

        if (notes.isEmpty()) {
          return notes
        }

        val items = emptyList<RecyclerItem>().toMutableList()
        var lastFolder = ""
        notes.forEach {
          val noteFolderId = it.note.folder
          if (lastFolder != noteFolderId) {
            val folder = instance.foldersDatabase().getByUUID(noteFolderId)
            if (folder !== null) {
              items.add(SelectorFolderRecyclerItem(this@SelectableNotesActivityBase, folder))
              lastFolder = noteFolderId
            }
          }
          items.add(it)
        }
        return items
      }

      override fun handle(notes: List<RecyclerItem>) {
        adapter.clearItems()

        if (notes.isEmpty()) {
          adapter.addItem(EmptyRecyclerItem())
        }

        notes.forEach {
          adapter.addItem(it)
        }
      }
    })

    findViewById<View>(R.id.back_button).setOnClickListener {
      onBackPressed()
    }
  }

  abstract fun getNotes(): List<Note>;

  open fun getLayoutUI(): Int = R.layout.activity_select_note

  fun setupRecyclerView() {
    val staggeredView = sAppPreferences.get(UISettingsOptionsBottomSheet.KEY_LIST_VIEW, false)
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = sAppPreferences.get(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = sAppPreferences.get(SettingsOptionsBottomSheet.KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(STORE_KEY_LINE_COUNT, sNoteItemLineCount)

    adapter = NoteAppAdapter(this, getSelectableRecyclerItemControllerList(staggeredView, isTablet))
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
      .setView(this, R.id.recycler_view)
      .setAdapter(adapter)
      .setLayoutManager(getLayoutManager(staggeredView, isTablet))
      .build()
  }

  override fun notifyThemeChange() {
    setSystemTheme()

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = sAppTheme.get(ThemeColorType.TOOLBAR_ICON);
    findViewById<ImageView>(R.id.back_button).setColorFilter(toolbarIconColor)
    findViewById<TextView>(R.id.toolbar_title).setTextColor(toolbarIconColor)
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    if (isTabletView) {
      return StaggeredGridLayoutManager(2, GridLayout.VERTICAL)
    }
    return if (isStaggeredView)
      StaggeredGridLayoutManager(2, GridLayout.VERTICAL)
    else
      LinearLayoutManager(this)
  }
}
