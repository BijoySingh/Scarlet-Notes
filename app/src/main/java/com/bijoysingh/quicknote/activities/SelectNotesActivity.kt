package com.bijoysingh.quicknote.activities

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.SelectedNoteOptionsBottomSheet
import com.bijoysingh.quicknote.database.notesDB
import com.bijoysingh.quicknote.database.utils.getFullText
import com.bijoysingh.quicknote.utils.HomeNavigationState
import com.bijoysingh.quicknote.utils.bind
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.database.room.note.Note

const val KEY_SELECT_EXTRA_MODE = "KEY_SELECT_EXTRA_MODE"
const val KEY_SELECT_EXTRA_NOTE_ID = "KEY_SELECT_EXTRA_NOTE_ID"

class SelectNotesActivity : SelectableNotesActivityBase() {

  val selectedNotes = ArrayList<Int>()
  var mode = ""

  val primaryFab: FloatingActionButton by bind(R.id.primary_fab_action)
  val secondaryFab: FloatingActionButton by bind(R.id.secondary_fab_action)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_notes)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      mode = extras.getString(KEY_SELECT_EXTRA_MODE, "")

      val noteId = getIntent().getIntExtra(KEY_SELECT_EXTRA_NOTE_ID, 0)
      if (noteId != 0) {
        selectedNotes.add(noteId)
      }
    }

    initUI()
  }

  override fun initUI() {
    super.initUI()
    primaryFab.setOnClickListener {
      runTextFunction {
        IntentUtils.ShareBuilder(applicationContext)
            .setChooserText(getString(R.string.share_using))
            .setText(it)
            .share()
      }
    }
    secondaryFab.setOnClickListener {
      SelectedNoteOptionsBottomSheet.openSheet(this, mode)
    }
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

  fun runNoteFunction(noteFunction: (Note) -> Unit) {
    for (noteId in selectedNotes) {
      val note = notesDB.getByID(noteId)
      if (note !== null) {
        noteFunction(note)
      }
    }
  }

  fun runTextFunction(textFunction: (String) -> Unit) {
    textFunction(getText())
  }

  override fun getLayoutUI() = R.layout.activity_select_notes

  override fun onNoteClicked(note: Note) {
    if (isNoteSelected(note)) {
      selectedNotes.remove(note.uid)
    } else {
      selectedNotes.add(note.uid)
    }
    adapter.notifyDataSetChanged()

    if (selectedNotes.isEmpty()) {
      onBackPressed()
    }
  }

  override fun notifyThemeChange() {
    super.notifyThemeChange()
  }

  override fun isNoteSelected(note: Note): Boolean = selectedNotes.contains(note.uid)

  override fun getNotes(): List<Note> = notesDB.getByNoteState(getMode(mode)).filter { note -> !note.locked }

  fun getText(): String {
    val builder = StringBuilder()
    for (noteId in selectedNotes) {
      builder.append(notesDB.getByID(noteId)?.getFullText())
      builder.append("\n\n---\n\n")
    }
    return builder.toString()
  }

  fun getMode(navigationState: String): Array<String> {
    return when (navigationState) {
      HomeNavigationState.FAVOURITE.name -> arrayOf(HomeNavigationState.FAVOURITE.name)
      HomeNavigationState.ARCHIVED.name -> arrayOf(HomeNavigationState.ARCHIVED.name)
      HomeNavigationState.TRASH.name -> arrayOf(HomeNavigationState.TRASH.name)
      else -> arrayOf(HomeNavigationState.DEFAULT.name, HomeNavigationState.FAVOURITE.name)
    }
  }
}
