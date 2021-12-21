package com.maubis.scarlet.base.export.sheet

import android.app.Dialog
import android.graphics.Typeface
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.export.support.ExternalFolderSync
import com.maubis.scarlet.base.export.support.sExternalFolderSync
import com.maubis.scarlet.base.export.support.sFolderSyncBackupLocked
import com.maubis.scarlet.base.export.support.sFolderSyncPath
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.separatorSpec
import com.maubis.scarlet.base.support.ui.ThemeColorType

class ExternalFolderSyncBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.import_export_layout_folder_sync_title)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .typeface(sAppTypeface.text())
          .textRes(R.string.import_export_layout_folder_sync_description)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(separatorSpec(componentContext).alpha(0.5f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_xlarge)
          .typeface(sAppTypeface.title())
          .textRes(R.string.import_export_layout_folder_sync_folder)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .textColor(sAppTheme.get(ThemeColorType.SECTION_HEADER)))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .text(sFolderSyncPath)
          .typeface(Typeface.MONOSPACE)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(separatorSpec(componentContext).alpha(0.5f))

    getOptions().forEach {
      if (it.visible) {
        component.child(OptionItemLayout.create(componentContext)
                          .option(it)
                          .onClick {
                            it.listener()
                            reset(componentContext.androidContext, dialog)
                          })
      }
    }

    component.child(BottomSheetBar.create(componentContext)
                      .primaryActionRes(
                        if (sExternalFolderSync) R.string.import_export_layout_folder_sync_disable else R.string.import_export_layout_folder_sync_enable)
                      .isActionNegative(sExternalFolderSync)
                      .onPrimaryClick {
                        sExternalFolderSync = !sExternalFolderSync
                        ExternalFolderSync.enable(componentContext.androidContext, sExternalFolderSync)
                        reset(componentContext.androidContext, dialog)
                      }
                      .paddingDip(YogaEdge.HORIZONTAL, 20f)
                      .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  fun getOptions(): List<LithoOptionsItem> {
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = { sFolderSyncBackupLocked = !sFolderSyncBackupLocked },
        isSelectable = true,
        selected = sFolderSyncBackupLocked
      ))
    return options
  }

}