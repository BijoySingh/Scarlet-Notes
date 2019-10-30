package com.maubis.scarlet.base.note.actions

import android.app.Dialog
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import android.widget.TextView
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.support.utils.removeMarkdownHeaders

fun Note.getTextToSpeechText(): String {
  val builder = StringBuilder()
  builder.append(Markdown.render(removeMarkdownHeaders(getFullText())), true)
  return builder.toString().trim { it <= ' ' }
}

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
    title.setTextColor(ApplicationBase.sAppTheme.get(ThemeColorType.SECONDARY_TEXT))

    val speakPlayPause = dialog.findViewById<ImageView>(R.id.speak_play_pause)
    speakPlayPause.setColorFilter(ApplicationBase.sAppTheme.get(ThemeColorType.TOOLBAR_ICON))
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
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun speak(note: Note) {
    when (OsVersionUtils.requiresTTSUtteranceId()) {
      true -> textToSpeech?.speak(note.getTextToSpeechText(), TextToSpeech.QUEUE_FLUSH, null, "NOTE")
      false -> textToSpeech?.speak(note.getTextToSpeechText(), TextToSpeech.QUEUE_FLUSH, null)
    }
  }

  override fun getBackgroundView(): Int = R.id.container_layout

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.speak_note_card)

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