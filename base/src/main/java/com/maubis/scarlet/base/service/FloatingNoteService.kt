package com.maubis.scarlet.base.service

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bsk.floatingbubblelib.FloatingBubbleConfig
import com.bsk.floatingbubblelib.FloatingBubblePermissions
import com.bsk.floatingbubblelib.FloatingBubbleService
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.copy
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.getDisplayTime
import com.maubis.scarlet.base.note.getFullTextForDirectMarkdownRender
import com.maubis.scarlet.base.note.getTextForSharing
import com.maubis.scarlet.base.note.getTitleForSharing
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.maybeThrow

class FloatingNoteService : FloatingBubbleService() {

  private var note: Note? = null
  private lateinit var description: TextView
  private lateinit var timestamp: TextView
  private lateinit var panel: View

  override fun getConfig(): FloatingBubbleConfig {
    return FloatingBubbleConfig.Builder()
      .bubbleIcon(ContextCompat.getDrawable(context, R.drawable.app_icon))
      .removeBubbleIcon(
        ContextCompat.getDrawable(
          context,
          com.bsk.floatingbubblelib.R.drawable.close_default_icon))
      .bubbleIconDp(72)
      .removeBubbleIconDp(72)
      .paddingDp(8)
      .borderRadiusDp(4)
      .physicsEnabled(false)
      .expandableColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .triangleColor(sAppTheme.get(ThemeColorType.BACKGROUND))
      .gravity(Gravity.END)
      .expandableView(loadView())
      .removeBubbleAlpha(0.7f)
      .build()
  }

  override fun onGetIntent(intent: Intent): Boolean {
    note = null
    if (intent.hasExtra(INTENT_KEY_NOTE_ID)) {
      note = notesDb.getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
    }
    return note != null
  }

  private fun loadView(): View {
    if (note == null) {
      note = NoteBuilder().emptyNote()
      stopSelf()
    }

    val rootView = getInflater().inflate(R.layout.layout_add_note_overlay, null)

    description = rootView.findViewById<View>(R.id.description) as TextView
    timestamp = rootView.findViewById<View>(R.id.timestamp) as TextView

    description.setTextColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))

    val noteItem = note!!

    val editButton = rootView.findViewById<View>(R.id.panel_edit_button) as ImageView
    editButton.setImageResource(R.drawable.ic_edit_white_48dp)
    editButton.setOnClickListener {
      try {
        val intent = Intent(context, CreateNoteActivity::class.java)
        intent.putExtra(INTENT_KEY_NOTE_ID, noteItem.uid)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
      stopSelf()
    }

    val shareButton = rootView.findViewById<View>(R.id.panel_share_button) as ImageView
    shareButton.setImageResource(R.drawable.ic_share_white_48dp)
    shareButton.setOnClickListener {
      getShareIntent(noteItem)
      stopSelf()
    }

    val copyButton = rootView.findViewById<View>(R.id.panel_copy_button) as ImageView
    copyButton.visibility = View.VISIBLE
    copyButton.setOnClickListener {
      noteItem.copy(context)
      setState(false)
    }

    panel = rootView.findViewById(R.id.panel_layout)
    panel.setBackgroundColor(sAppTheme.get(ThemeColorType.BACKGROUND))

    setNote(noteItem)
    return rootView
  }

  private fun getShareIntent(note: Note) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitleForSharing())
    sharingIntent.putExtra(Intent.EXTRA_TEXT, note.getTextForSharing())
    sharingIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(sharingIntent)
  }

  private fun setNote(note: Note) {
    val noteDescription = Markdown.render(note.getFullTextForDirectMarkdownRender(), true)
    description.text = noteDescription
    timestamp.text = note.getDisplayTime()
  }

  companion object {
    fun openNote(activity: Activity, note: Note?, finishOnOpen: Boolean) {
      if (FloatingBubblePermissions.requiresPermission(activity)) {
        FloatingBubblePermissions.startPermissionRequest(activity)
      } else {
        val intent = Intent(activity, FloatingNoteService::class.java)
        if (note != null) {
          intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
        }
        activity.startService(intent)
        if (finishOnOpen) activity.finish()
      }
    }
  }
}
