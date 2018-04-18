package com.bijoysingh.quicknote.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SortingOptionsBottomSheet
import com.bijoysingh.quicknote.activities.sheets.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.database.room.note.Note
import com.bijoysingh.quicknote.items.EmptyRecyclerItem
import com.bijoysingh.quicknote.items.NoteRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.sort
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder

abstract class SelectableNotesActivityBase : ThemedActivity(), INoteSelectorActivity {

  lateinit var recyclerView: RecyclerView
  lateinit var adapter: NoteAppAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayoutUI())
  }

  open fun initUI() {
    notifyThemeChange()
    setupRecyclerView()

    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
      override fun run(): List<NoteRecyclerItem> {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        return sort(getNotes(), sorting)
            .map { NoteRecyclerItem(this@SelectableNotesActivityBase, it) }
      }

      override fun handle(notes: List<NoteRecyclerItem>) {
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
    val staggeredView = userPreferences().get(UISettingsOptionsBottomSheet.KEY_LIST_VIEW, false)
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = userPreferences().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = userPreferences().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(LineCountBottomSheet.KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount())

    adapter = NoteAppAdapter(this, RecyclerItem.getSelectableList(staggeredView, isTablet))
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

    val toolbarIconColor = appTheme().get(ThemeColorType.TOOLBAR_ICON);
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
