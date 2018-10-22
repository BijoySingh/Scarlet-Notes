package com.maubis.scarlet.base.main.activity

import android.support.v4.content.ContextCompat
import android.view.View
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.ui.ThemedActivity
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

interface ITutorialActivity {

  fun showHints(): Boolean

  fun shouldShowHint(key: String): Boolean

  fun showHint(key: String)

  fun markHintShown(key: String)
}

fun createHint(activity: ThemedActivity,
               anchorView: View,
               title: String = "",
               subtitle: String = "") {
  MaterialTapTargetPrompt.Builder(activity)
      .setTarget(anchorView)
      .setBackgroundColour(ContextCompat.getColor(activity, R.color.material_teal_700))
      .setFocalColour(ContextCompat.getColor(activity, R.color.material_teal_800))
      .setPrimaryText(title)
      .setSecondaryText(subtitle)
      .setPromptStateChangeListener(MaterialTapTargetPrompt.PromptStateChangeListener { _, state ->
        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
          // User has pressed the prompt target
        }
      })
      .show()
}