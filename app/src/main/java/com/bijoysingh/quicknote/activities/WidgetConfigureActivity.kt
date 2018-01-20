package com.bijoysingh.quicknote.activities

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.GridLayout.VERTICAL
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.bijoysingh.quicknote.activities.sheets.SortingOptionsBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Widget
import com.bijoysingh.quicknote.items.EmptyRecyclerItem
import com.bijoysingh.quicknote.items.NoteRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.bijoysingh.quicknote.service.NoteWidgetProvider
import com.bijoysingh.quicknote.utils.NoteState
import com.bijoysingh.quicknote.utils.sort
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.github.bijoysingh.starter.util.TextUtils

class WidgetConfigureActivity : ThemedActivity(), INoteSelectorActivity {

  lateinit var recyclerView: RecyclerView
  lateinit var adapter: NoteAppAdapter
  lateinit var store: DataStore
  var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_widget_configure)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    store = DataStore.get(this)
    requestSetNightMode(store.get(ThemedActivity.getKey(), false))
    setupRecyclerView()

    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<Note>> {
      override fun run(): List<Note> {
        val sorting = SortingOptionsBottomSheet.getSortingState(store)
        return sort(Note.db(this@WidgetConfigureActivity).getByNoteState(
            arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name, NoteState.ARCHIVED.name)),
            sorting)
      }

      override fun handle(notes: List<Note>) {
        adapter.clearItems()

        if (notes.isEmpty()) {
          adapter.addItem(EmptyRecyclerItem())
        }

        for (note in notes) {
          adapter.addItem(NoteRecyclerItem(note))
        }
      }
    })
  }

  fun setupRecyclerView() {
    val staggeredView = store.get(KEY_LIST_VIEW, false)
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = store.get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = store.get(KEY_MARKDOWN_HOME_ENABLED, false)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount(store))

    adapter = NoteAppAdapter(this, RecyclerItem.getSelectableList(staggeredView, isTablet))
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
        .build()
  }

  override fun onNoteClicked(note: Note) {
    val widget = Widget(appWidgetId, note.uuid)
    Widget.db(this).insert(widget)
    createWidget(widget)
  }

  override fun isNoteSelected(note: Note): Boolean {
    return false
  }

  override fun notifyNightModeChange() {
    setSystemTheme()

    val containerLayout = findViewById<View>(R.id.container_layout)
    containerLayout.setBackgroundColor(getThemeColor())

    val toolbarIconColor = getColor(R.color.material_blue_grey_700, R.color.light_secondary_text)
    findViewById<ImageView>(R.id.back_button).setColorFilter(toolbarIconColor)
    findViewById<TextView>(R.id.toolbar_title).setTextColor(toolbarIconColor)
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    if (isTabletView) {
      return StaggeredGridLayoutManager(2, VERTICAL)
    }
    return if (isStaggeredView)
      StaggeredGridLayoutManager(2, VERTICAL)
    else
      LinearLayoutManager(this)
  }

  fun createWidget(widget: Widget) {
    createNoteWidget(this, widget)

    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }

  companion object {
    fun createNoteWidget(context: Context, widget: Widget) {
      val note = Note.db(context).getByUUID(widget.noteUUID)
      if (note === null) {
        return
      }

      val intent = ViewAdvancedNoteActivity.getIntent(context, note)
      val pendingIntent = PendingIntent.getActivity(context, 5000 + note.uid, intent, 0)

      val views = RemoteViews(context.getPackageName(), R.layout.widget_layout)

      val noteTitle = note.getTitle()
      views.setViewVisibility(R.id.title, if (TextUtils.isNullOrEmpty(noteTitle)) GONE else VISIBLE)
      views.setTextViewText(R.id.title, noteTitle)
      views.setTextViewText(
          R.id.description,
          note.getLockedText(context, false))
      views.setInt(R.id.container_layout, "setBackgroundColor", note.color);

      views.setOnClickPendingIntent(R.id.title, pendingIntent)
      views.setOnClickPendingIntent(R.id.description, pendingIntent)
      views.setOnClickPendingIntent(R.id.container_layout, pendingIntent)

      val appWidgetManager = AppWidgetManager.getInstance(context)
      appWidgetManager.updateAppWidget(widget.widgetId, views)
    }

    fun notifyNoteChange(context: Context?, note: Note?) {
      if (context === null || note === null) {
        return
      }

      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
          ComponentName(application, NoteWidgetProvider::class.java))
      val widgets = Widget.db(context).getByNote(note.uuid)

      val widgetIds = ArrayList<Int>()
      for (widget in widgets) {
        if (ids.contains(widget.widgetId)) {
          widgetIds.add(widget.widgetId)
        }
      }

      if (widgetIds.isEmpty()) {
        return
      }

      val intentIds = IntArray(widgetIds.size, { index -> widgetIds[index] })

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intentIds);
      context.sendBroadcast(intent);
    }
  }
}
