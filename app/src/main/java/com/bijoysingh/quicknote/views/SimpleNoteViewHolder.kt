package com.bijoysingh.quicknote.views

import android.app.Activity
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView

import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.bijoysingh.quicknote.formats.NoteType

import java.util.ArrayList

/**
 * The note view holder
 * Created by bijoy on 5/4/16.
 */
class SimpleNoteViewHolder(activity: Activity) {
  var title: EditText
  var description: EditText
  var timestamp: TextView

  init {
    title = activity.findViewById<EditText>(R.id.title)
    description = activity.findViewById<EditText>(R.id.description)
    timestamp = activity.findViewById<TextView>(R.id.timestamp)

    title.imeOptions = EditorInfo.IME_ACTION_DONE
    title.setRawInputType(InputType.TYPE_CLASS_TEXT)
    title.setOnEditorActionListener(TextView.OnEditorActionListener { view, actionId, event ->
      if (event == null) {
        if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
          return@OnEditorActionListener false
        }
      } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
        if (event.action != KeyEvent.ACTION_DOWN) {
          return@OnEditorActionListener true
        }
      } else {
        return@OnEditorActionListener false
      }
      description.requestFocus()
      true
    })
  }

  fun setNote(item: Note) {
    title.setText(item.getTitle())
    description.setText(item.text)
    timestamp.text = item.displayTimestamp
  }

  fun getNote(item: Note): Note {
    val formats = ArrayList<Format>()
    formats.add(Format(FormatType.HEADING, title.text.toString()))
    formats.add(Format(FormatType.TEXT, description.text.toString()))
    item.title = NoteType.NOTE.name
    item.description = Format.getNote(formats)
    return item
  }
}
