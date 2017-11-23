package com.bijoysingh.quicknote.activities.external

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.Note
import com.github.bijoysingh.starter.json.SafeJson

class ExportNotes : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_import_export)
    val notes = getNotes()
  }

  fun getNotes(): String {
    val notes = Note.db(this).all
    val exportableNotes = ArrayList<String>()
    for (note in notes) {
      exportableNotes.add(ExportableNote(note).toBase64String())
    }
    val mapping = HashMap<String, ArrayList<String>>()
    mapping[ExportableNote.KEY_NOTES] = exportableNotes
    val json = SafeJson(mapping)
    return json.toString()
  }
}
