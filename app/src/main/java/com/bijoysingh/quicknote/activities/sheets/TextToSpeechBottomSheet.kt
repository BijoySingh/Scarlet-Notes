package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.maubis.scarlet.base.database.room.note.Note
import com.bijoysingh.quicknote.database.utils.getUnreliablyStrippedText
import com.bijoysingh.quicknote.utils.ThemeColorType

class TextToSpeechBottomSheet : ThemedBottomSheetFragment() {

  var note: Note? = null
  var textToSpeech: TextToSpeech? = null

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null || note == null) {
      return
    }

    val nonNullNote = note!!
    val title = dialog.findViewById<TextView>(R.id.options_title)
    title.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))

    val speakPlayPause = dialog.findViewById<ImageView>(R.id.speak_play_pause)
    speakPlayPause.setColorFilter(appTheme().get(ThemeColorType.TOOLBAR_ICON))
    speakPlayPause.setOnClickListener {
      val tts = textToSpeech
      if (tts === null) {
        return@setOnClickListener
      }

      when {
        tts.isSpeaking -> {
          tts.stop()
          speakPlayPause.setImageResource(R.drawable.ic_action_play_sound)
        }
        else -> {
          speak(nonNullNote)
          speakPlayPause.setImageResource(R.drawable.ic_action_stop)
        }
      }
    }

    textToSpeech = TextToSpeech(themedContext(), TextToSpeech.OnInitListener {
      speak(nonNullNote)
      speakPlayPause.setImageResource(R.drawable.ic_action_stop)
    })
  }

  fun speak(note: Note) {
    if (Build.VERSION.SDK_INT >= 21) {
      textToSpeech?.speak(note.getUnreliablyStrippedText(themedContext()), TextToSpeech.QUEUE_FLUSH, null, "NOTE")
    } else {
      textToSpeech?.speak(note.getUnreliablyStrippedText(themedContext()), TextToSpeech.QUEUE_FLUSH, null)
    }
  }

  override fun getBackgroundView(): Int = R.id.container_layout

  override fun getLayout(): Int = R.layout.bottom_sheet_speak_note

  override fun onPause() {
    super.onPause()
    textToSpeech?.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    textToSpeech?.shutdown()
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = TextToSpeechBottomSheet()
      sheet.note = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}