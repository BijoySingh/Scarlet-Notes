package com.bijoysingh.quicknote.activities

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.utils.HomeNavigationState
import com.bijoysingh.quicknote.utils.NoteState
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils

const val KEY_SELECT_EXTRA_MODE = "KEY_SELECT_EXTRA_MODE"
const val KEY_SELECT_EXTRA_NOTE_ID = "KEY_SELECT_EXTRA_NOTE_ID"

class SelectNotesActivity : SelectableNotesActivityBase() {

  val selectedNotes = ArrayList<Int>()
  var mode = ""

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

    val share = findViewById<View>(R.id.share_button) as ImageView
    share.setOnClickListener {
      IntentUtils.ShareBuilder(applicationContext)
          .setChooserText(getString(R.string.share_using))
          .setText(getText())
          .share()

    }

    val delete = findViewById<View>(R.id.delete_button) as ImageView
    delete.setImageResource(if (mode === HomeNavigationState.TRASH.name) R.drawable.ic_delete_permanently else R.drawable.ic_delete_white_48dp)
    delete.setOnClickListener {
      runNoteFunction {
        if (mode === HomeNavigationState.TRASH.name) {
          it.delete(this)
          return@runNoteFunction
        }
        it.mark(this, NoteState.TRASH)
      }
      finish()
    }

    val copy = findViewById<View>(R.id.copy_button) as ImageView
    copy.setOnClickListener {
      TextUtils.copyToClipboard(this, getText())
      finish()
    }

    val favourite = findViewById<View>(R.id.favourite_button) as ImageView
    favourite.setImageResource(if (mode === HomeNavigationState.FAVOURITE.name) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
    favourite.setOnClickListener {
      runNoteFunction {
        it.mark(this, if (mode === HomeNavigationState.FAVOURITE.name) NoteState.DEFAULT else NoteState.FAVOURITE)
      }
      finish()
    }

    val archive = findViewById<View>(R.id.archive_button) as ImageView
    archive.setOnClickListener {
      runNoteFunction {
        it.mark(this, if (mode === HomeNavigationState.ARCHIVED.name) NoteState.DEFAULT else NoteState.ARCHIVED)
      }
      finish()
    }
  }

  fun runNoteFunction(noteFunction: (Note) -> Unit) {
    for (noteId in selectedNotes) {
      val note = Note.db(this).getByID(noteId)
      noteFunction(note)
    }
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

  override fun notifyNightModeChange() {
    super.notifyNightModeChange()

    val toolbarIconColor = ContextCompat.getColor(
        this, if (isNightMode) R.color.white else R.color.material_blue_grey_700)

    val share = findViewById<View>(R.id.share_button) as ImageView
    share.setColorFilter(toolbarIconColor)

    val delete = findViewById<View>(R.id.delete_button) as ImageView
    delete.setColorFilter(toolbarIconColor)

    val copy = findViewById<View>(R.id.copy_button) as ImageView
    copy.setColorFilter(toolbarIconColor)

    val favourite = findViewById<View>(R.id.favourite_button) as ImageView
    favourite.setColorFilter(toolbarIconColor)

    val archive = findViewById<View>(R.id.archive_button) as ImageView
    archive.setColorFilter(toolbarIconColor)
  }

  override fun isNoteSelected(note: Note): Boolean = selectedNotes.contains(note.uid)

  override fun getNotes(): List<Note> = Note.db(this).getByNoteState(getMode(mode))

  fun getText(): String {
    val builder = StringBuilder()
    for (noteId in selectedNotes) {
      builder.append(Note.db(this).getByID(noteId))
      builder.append("\n---\n")
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
