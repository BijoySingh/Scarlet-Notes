package com.maubis.scarlet.base.security.sheets

import android.app.Dialog
import android.text.InputType
import android.text.Layout
import android.view.inputmethod.EditorInfo
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.FromEvent
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.TextChangedEvent
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.security.controller.PinLockController
import com.maubis.scarlet.base.security.controller.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.controller.PinLockController.needsLockCheck
import com.maubis.scarlet.base.settings.sheet.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sheet.sSecurityCode
import com.maubis.scarlet.base.settings.sheet.sSecurityFingerprintEnabled
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.getEditorActionListener

data class PincodeSheetData(
  val title: Int,
  val actionTitle: Int,
  val onSuccess: () -> Unit,
  val onFailure: () -> Unit = {},
  val isFingerprintEnabled: Boolean = false,
  val onActionClicked: (String) -> Unit = { password ->
    when {
      password != "" && password == sSecurityCode -> {
        PinLockController.notifyPinVerified()
        onSuccess()
      }
      else -> onFailure()
    }
  },
  val isRemoveButtonEnabled: Boolean = false,
  val onRemoveButtonClick: () -> Unit = {})

@LayoutSpec
object PincodeSheetViewSpec {

  private var passcodeEntered = ""

  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop data: PincodeSheetData,
    @Prop dismiss: () -> Unit): Component {
    val editBackground = when {
      sAppTheme.isNightTheme() -> R.drawable.light_secondary_rounded_bg
      else -> R.drawable.secondary_rounded_bg
    }

    val component = Column.create(context)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(context)
          .textRes(data.title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(context)
          .typeface(sAppTypeface.text())
          .textSizeRes(R.dimen.font_size_large)
          .textRes(R.string.app_lock_details)
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(
        EditText.create(context)
          .backgroundRes(editBackground)
          .textSizeRes(R.dimen.font_size_xlarge)
          .minWidthDip(128f)
          .maxLength(4)
          .alignSelf(YogaAlign.CENTER)
          .hint("****")
          .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
          .textAlignment(Layout.Alignment.ALIGN_CENTER)
          .typeface(sAppTypeface.text())
          .textColor(sAppTheme.get(ThemeColorType.PRIMARY_TEXT))
          .paddingDip(YogaEdge.HORIZONTAL, 22f)
          .paddingDip(YogaEdge.VERTICAL, 6f)
          .marginDip(YogaEdge.VERTICAL, 8f)
          .imeOptions(EditorInfo.IME_ACTION_DONE)
          .editorActionListener(getEditorActionListener({
                                                          data.onActionClicked(passcodeEntered)
                                                          dismiss()
                                                          true
                                                        }))
          .textChangedEventHandler(PincodeSheetView.onTextChangeListener(context)))
      .child(
        Row.create(context)
          .alignItems(YogaAlign.CENTER)
          .paddingDip(YogaEdge.HORIZONTAL, 8f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .child(
            when {
              data.isFingerprintEnabled -> Image.create(context)
                .drawableRes(R.drawable.ic_option_fingerprint)
                .heightDip(36f)
              else -> null
            }
          )
          .child(
            when {
              data.isRemoveButtonEnabled -> Text.create(context)
                .textSizeRes(R.dimen.font_size_large)
                .textColor(sAppTheme.get(ThemeColorType.HINT_TEXT))
                .textRes(R.string.security_sheet_button_remove)
                .textAlignment(Layout.Alignment.ALIGN_CENTER)
                .paddingDip(YogaEdge.VERTICAL, 12f)
                .paddingDip(YogaEdge.HORIZONTAL, 20f)
                .typeface(sAppTypeface.title())
                .clickHandler(PincodeSheetView.onRemoveClick(context))
              else -> null
            }
          )
          .child(EmptySpec.create(context).flexGrow(1f))
          .child(
            Text.create(context)
              .backgroundRes(R.drawable.accent_rounded_bg)
              .textSizeRes(R.dimen.font_size_large)
              .textColorRes(R.color.white)
              .textRes(data.actionTitle)
              .textAlignment(Layout.Alignment.ALIGN_CENTER)
              .paddingDip(YogaEdge.VERTICAL, 12f)
              .paddingDip(YogaEdge.HORIZONTAL, 20f)
              .typeface(sAppTypeface.title())
              .clickHandler(PincodeSheetView.onActionClick(context))))
    return component.build()
  }

  @OnEvent(TextChangedEvent::class)
  fun onTextChangeListener(context: ComponentContext, @FromEvent text: String) {
    passcodeEntered = text
  }

  @OnEvent(ClickEvent::class)
  fun onActionClick(
    context: ComponentContext,
    @Prop data: PincodeSheetData,
    @Prop dismiss: () -> Unit) {
    data.onActionClicked(passcodeEntered)
    passcodeEntered = ""
    dismiss()
  }

  @OnEvent(ClickEvent::class)
  fun onRemoveClick(
    context: ComponentContext,
    @Prop data: PincodeSheetData,
    @Prop dismiss: () -> Unit) {
    data.onRemoveButtonClick()
    passcodeEntered = ""
    dismiss()
  }
}

class PincodeBottomSheet : LithoBottomSheet() {
  var data = PincodeSheetData(
    title = R.string.no_pincode_sheet_title,
    actionTitle = R.string.no_pincode_sheet_details,
    onSuccess = {})

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    return PincodeSheetView.create(componentContext)
      .data(data)
      .dismiss { dismiss() }
      .build()
  }

  override fun onResume() {
    super.onResume()
    if (data.isFingerprintEnabled) {
      Reprint.authenticate(object : AuthenticationListener {
        override fun onSuccess(moduleTag: Int) {
          data.onSuccess()
          dismiss()
        }

        override fun onFailure(
          failureReason: AuthenticationFailureReason?, fatal: Boolean, errorMessage: CharSequence?, moduleTag: Int, errorCode: Int) {
        }
      })
    }
  }

  override fun onPause() {
    super.onPause()
    if (data.isFingerprintEnabled) {
      Reprint.cancelAuthentication()
    }
  }
}

fun openCreateSheet(
  activity: ThemedActivity,
  onCreateSuccess: () -> Unit) {

  openSheet(activity, PincodeBottomSheet().apply {
    data = PincodeSheetData(
      title = R.string.security_sheet_enter_new_pin_title,
      actionTitle = R.string.security_sheet_button_set,
      isFingerprintEnabled = false,
      isRemoveButtonEnabled = true,
      onRemoveButtonClick = {
        sSecurityCode = ""
        sSecurityAppLockEnabled = false
        sNoPinSetupNoticeShown = false
        onCreateSuccess()

        if (activity is MainActivity) {
          activity.setupData()
        }
      },
      onActionClicked = { password: String ->
        if (password.length == 4 && password.toIntOrNull() !== null) {
          sSecurityCode = password
          onCreateSuccess()
        }
      },
      onSuccess = {}
    )
  })
}

fun openVerifySheet(
  activity: ThemedActivity,
  onVerifySuccess: () -> Unit,
  onVerifyFailure: () -> Unit = {}) {
  openSheet(activity, PincodeBottomSheet().apply {
    data = PincodeSheetData(
      title = R.string.security_sheet_enter_current_pin_title,
      actionTitle = R.string.security_sheet_button_verify,
      onSuccess = onVerifySuccess,
      onFailure = onVerifyFailure,
      isFingerprintEnabled = Reprint.hasFingerprintRegistered() && sSecurityFingerprintEnabled
    )
  })
}

fun openUnlockSheet(
  activity: ThemedActivity,
  onUnlockSuccess: () -> Unit,
  onUnlockFailure: () -> Unit) {
  if (!isPinCodeEnabled()) {
    if (sNoPinSetupNoticeShown) {
      onUnlockSuccess()
      return
    }
    openSheet(activity, NoPincodeBottomSheet().apply {
      this.onSuccess = onUnlockSuccess
    })
    return
  }

  if (!needsLockCheck()) {
    return onUnlockSuccess()
  }
  openSheet(activity, PincodeBottomSheet().apply {
    data = PincodeSheetData(
      title = R.string.security_sheet_enter_pin_to_unlock_title,
      actionTitle = R.string.security_sheet_button_unlock,
      onSuccess = onUnlockSuccess,
      onFailure = onUnlockFailure,
      isFingerprintEnabled = Reprint.hasFingerprintRegistered() && sSecurityFingerprintEnabled
    )
  })
}