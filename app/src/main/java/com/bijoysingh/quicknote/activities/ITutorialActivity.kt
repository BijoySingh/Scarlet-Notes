package com.bijoysingh.quicknote.activities

import android.view.View
import com.bijoysingh.quicknote.activities.ThemedActivity
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

interface ITutorialActivity {

  fun showHints(): Boolean

  fun shouldShowHint(key: String): Boolean

  fun showHint(key: String)

  fun markHintShown(key: String)
}

fun createHint(activity: ThemedActivity,
               anchorView: View,
               title: String =  "",
               subtitle: String = "") {
  MaterialTapTargetPrompt.Builder(activity)
      .setTarget(anchorView)
      .setPrimaryText(title)
      .setSecondaryText(subtitle)
      .setPromptStateChangeListener(MaterialTapTargetPrompt.PromptStateChangeListener { _, state ->
        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
          // User has pressed the prompt target
        }
      })
      .show()
}