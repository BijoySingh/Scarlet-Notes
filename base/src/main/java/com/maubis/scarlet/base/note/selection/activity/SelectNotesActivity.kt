package com.maubis.scarlet.base.note.selection.activity

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.github.bijoysingh.starter.util.IntentUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.main.HomeNavigationMode
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.selection.sheet.SelectedNotesOptionsBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.bind

const val KEY_SELECT_EXTRA_MODE = "KEY_SELECT_EXTRA_MODE"
const val KEY_SELECT_EXTRA_NOTE_ID = "KEY_SELECT_EXTRA_NOTE_ID"

class SelectNotesActivity : SelectableNotesActivityBase() {

  val selectedNotes = HashMap<Int, Note>()
  val orderingNoteIds = ArrayList<Int>()

  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_notes)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      val noteId = getIntent().getIntExtra(KEY_SELECT_EXTRA_NOTE_ID, 0)
      if (noteId != 0) {
        val note = notesDb.getByID(noteId)
        if (note !== null) {
          orderingNoteIds.add(noteId)
          selectedNotes.put(noteId, note)
        }
      }
    }

    initUI()
  }

  override fun initUI() {
    super.initUI()
    primaryFab.setOnClickListener {
      runTextFunction { text ->
        IntentUtils.ShareBuilder(this)
          .setChooserText(getString(R.string.share_using))
          .setText(text)
          .share()
      }
    }
    secondaryFab.setOnClickListener {
      openSheet(this, SelectedNotesOptionsBottomSheet())
    }
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
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

  fun runNoteFunction(noteFunction: (Note) -> Unit) {
    for (note in selectedNotes.values) {
      noteFunction(note)
    }
  }

  fun runTextFunction(textFunction: (String) -> Unit) {
    textFunction(getText())
  }

  fun refreshSelectedNotes() {
    for (key in selectedNotes.keys.toList()) {
      val note = notesDb.getByID(key)
      if (note === null) {
        selectedNotes.remove(key)
        continue
      }
      selectedNotes[key] = note
    }
  }

  fun getAllSelectedNotes() = selectedNotes.values

  override fun getLayoutUI() = R.layout.activity_select_notes

  override fun onNoteClicked(note: Note) {
    if (isNoteSelected(note)) {
      selectedNotes.remove(note.uid)
      orderingNoteIds.remove(note.uid)
    } else {
      selectedNotes.put(note.uid, note)
      orderingNoteIds.add(note.uid)
    }
    adapter.notifyDataSetChanged()

    if (selectedNotes.isEmpty()) {
      onBackPressed()
    }
  }

  override fun notifyThemeChange() {
    super.notifyThemeChange()
  }

  override fun isNoteSelected(note: Note): Boolean = orderingNoteIds.contains(note.uid)

  override fun getNotes(): List<Note> = notesDb.getAll()

  fun getOrderedSelectedNotes(): List<Note> {
    val notes = ArrayList<Note>()
    for (noteId in orderingNoteIds) {
      val note = selectedNotes[noteId]
      if (note === null) {
        continue
      }
      notes.add(note)
    }
    return notes
  }

  fun getText(): String {
    val builder = StringBuilder()
    for (note in getOrderedSelectedNotes()) {
      builder.append(note.getFullText())
      builder.append("\n\n---\n\n")
    }
    return builder.toString()
  }

  fun getMode(navigationMode: String): Array<String> {
    return when (navigationMode) {
      HomeNavigationMode.FAVOURITE.name -> arrayOf(HomeNavigationMode.FAVOURITE.name)
      HomeNavigationMode.ARCHIVED.name -> arrayOf(HomeNavigationMode.ARCHIVED.name)
      HomeNavigationMode.TRASH.name -> arrayOf(HomeNavigationMode.TRASH.name)
      else -> arrayOf(HomeNavigationMode.DEFAULT.name, HomeNavigationMode.FAVOURITE.name)
    }
  }
}
