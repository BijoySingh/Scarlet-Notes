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
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.main.recycler.EmptyRecyclerItem
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.note.recycler.NoteRecyclerItem
import com.maubis.scarlet.base.note.recycler.getSelectableRecyclerItemControllerList
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity

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

    MultiAsyncTask.execute(object : MultiAsyncTask.Task<List<NoteRecyclerItem>> {
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
    val staggeredView = CoreConfig.instance.store().get(UISettingsOptionsBottomSheet.KEY_LIST_VIEW, false)
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = CoreConfig.instance.store().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = CoreConfig.instance.store().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(LineCountBottomSheet.KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount())

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

    val toolbarIconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON);
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
