package com.maubis.scarlet.base

import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.settings.sheet.ThemeColorPickerBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.sAppTheme

const val INTENT_KEY_ADDITIONAL_ACTION = "additional_action"

enum class MainActivityActions {
  NIL,
  COLOR_PICKER;

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
    MainActivityActions.NIL -> {}
    MainActivityActions.COLOR_PICKER -> {
      openSheet(this, ThemeColorPickerBottomSheet().apply {
        this.onThemeChange = { theme ->
          if (sAppTheme != theme.name) {
            sAppTheme = theme.name
            ApplicationBase.instance.themeController().notifyChange(activity)
            activity.startActivity(MainActivityActions.COLOR_PICKER.intent(activity))
            activity.finish()
          }
        }
      })
    }
  }
}