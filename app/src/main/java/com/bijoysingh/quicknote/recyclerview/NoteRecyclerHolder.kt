package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity.NOTE_ID
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet
import com.bijoysingh.quicknote.activities.sheets.NoteOptionsBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.github.bijoysingh.starter.prefs.DataStore

class NoteRecyclerHolder(context: Context, view: View) : NoteRecyclerViewHolderBase(context, view) {

  private val activity = context as MainActivity

  override fun viewClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { openNote(note) })
  }

  override fun viewLongClick(note: Note, extra: Bundle?) {
    NoteOptionsBottomSheet.openSheet(activity, note)
  }

  override fun deleteIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { activity.moveItemToTrashOrDelete(note) })
  }

  override fun shareIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { note.share(context) })
  }

  override fun editIconClick(note: Note, extra: Bundle?) {
    note.edit(context)
  }

  override fun copyIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { note.copy(context) })
  }

  override fun moreOptionsIconClick(note: Note, extra: Bundle?) {
    NoteOptionsBottomSheet.openSheet(activity, note)
  }

  private fun actionOrUnlockNote(data: Note, runnable: Runnable) {
    if (context is ThemedActivity && data.locked) {
      EnterPincodeBottomSheet.openUnlockSheet(
          context as ThemedActivity,
          object : EnterPincodeBottomSheet.PincodeSuccessListener {
            override fun onFailure() {
              actionOrUnlockNote(data, runnable)
            }

            override fun onSuccess() {
              runnable.run()
            }
          },
          DataStore.get(context))
      return
    } else if (data.locked) {
      return
    }
    runnable.run()
  }

  private fun openNote(data: Note) {
    val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
    intent.putExtra(NOTE_ID, data.uid)
    intent.putExtra(ThemedActivity.getKey(), (context as ThemedActivity).isNightMode)
    context.startActivity(intent)
  }
}
