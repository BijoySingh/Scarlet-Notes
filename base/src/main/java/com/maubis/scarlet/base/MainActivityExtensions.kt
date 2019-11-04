package com.maubis.scarlet.base

import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.settings.sheet.ThemeColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.TypefacePickerBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.font.sPreferenceTypeface
import com.maubis.scarlet.base.support.ui.sThemeLabel

const val INTENT_KEY_ADDITIONAL_ACTION = "additional_action"

enum class MainActivityActions {
  NIL,
  COLOR_PICKER,
  TYPEFACE_PICKER;

  fun intent(context: Context): Intent {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra(INTENT_KEY_ADDITIONAL_ACTION, this.name)
    return intent
  }
}

fun MainActivity.handleIntent() {
  val actionFromIntent = intent.getStringExtra(INTENT_KEY_ADDITIONAL_ACTION)
  if (actionFromIntent === null || actionFromIntent.isEmpty()) {
    return
  }

  val action = try {
    MainActivityActions.valueOf(actionFromIntent)
  } catch (exception: Exception) {
    null
  }

  if (action === null) {
    return
  }
  performAction(action)
}

fun MainActivity.performAction(action: MainActivityActions) {
  val activity = this
  when (action) {
    MainActivityActions.NIL -> {
    }
    MainActivityActions.COLOR_PICKER -> {
      openSheet(this, ThemeColorPickerBottomSheet().apply {
        this.onThemeChange = { theme ->
          if (sThemeLabel != theme.name) {
            sThemeLabel = theme.name
            sAppTheme.notifyChange(activity)
            activity.startActivity(MainActivityActions.COLOR_PICKER.intent(activity))
            activity.finish()
          }
        }
      })
    }
    MainActivityActions.TYPEFACE_PICKER -> {
      openSheet(this, TypefacePickerBottomSheet().apply {
        this.onTypefaceChange = { typeface ->
          if (sPreferenceTypeface != typeface.name) {
            sPreferenceTypeface = typeface.name
            sAppTypeface.notifyChange(activity)
            activity.startActivity(MainActivityActions.TYPEFACE_PICKER.intent(activity))
            activity.finish()
          }
        }
      })
    }
  }
}