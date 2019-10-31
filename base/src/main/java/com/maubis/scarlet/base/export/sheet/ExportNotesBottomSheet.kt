package com.maubis.scarlet.base.export.sheet

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.content.FileProvider
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.export.support.GenericFileProvider
import com.maubis.scarlet.base.export.support.NoteExporter
import com.maubis.scarlet.base.export.support.PermissionUtils
import com.maubis.scarlet.base.export.support.sAutoBackupMode
import com.maubis.scarlet.base.export.support.sBackupLockedNotes
import com.maubis.scarlet.base.export.support.sBackupMarkdown
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.separatorSpec
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.Flavor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val NOTES_EXPORT_FOLDER
  get() = when (ApplicationBase.instance.appFlavor()) {
    Flavor.NONE -> "MaterialNotes"
    Flavor.LITE -> "Scarlet"
    Flavor.PRO -> "ScarletPro"
  }
val NOTES_EXPORT_FILENAME = "manual_backup"

class ExportNotesBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = componentContext.androidContext as ThemedActivity

    val file = NoteExporter().getOrCreateManualExportFile()
    val filenameRender = "${file?.parentFile?.name}/${file?.name}"

    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.import_export_layout_exporting)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeRes(R.dimen.font_size_large)
          .text(filenameRender)
          .typeface(Typeface.MONOSPACE)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(separatorSpec(componentContext).alpha(0.5f))

    getOptions(componentContext).forEach {
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
                      .primaryActionRes(R.string.import_export_layout_exporting_done)
                      .onPrimaryClick {
                        GlobalScope.launch {
                          val notes = NoteExporter().getExportContent()
                          val success = NoteExporter().saveToManualExportFile(notes)
                          GlobalScope.launch(Dispatchers.Main) {
                            ToastHelper.show(
                              activity, if (success) R.string.import_export_layout_exported else R.string.import_export_layout_export_failed)
                            dismiss()
                          }
                        }
                      }
                      .secondaryActionRes(R.string.import_export_layout_exporting_share)
                      .onSecondaryClick {
                        GlobalScope.launch {
                          val notes = NoteExporter().getExportContent()
                          NoteExporter().saveToManualExportFile(notes)

                          if (file == null || !file.exists()) {
                            return@launch
                          }

                          val uri = FileProvider.getUriForFile(activity, GenericFileProvider.PROVIDER, file)

                          val intent = Intent(Intent.ACTION_SEND)
                          intent.type = "text/plain"
                          intent.putExtra(Intent.EXTRA_STREAM, uri)
                          startActivity(Intent.createChooser(intent, getString(R.string.share_using)))

                          GlobalScope.launch(Dispatchers.Main) {
                            dismiss()
                          }
                        }
                      }
                      .paddingDip(YogaEdge.HORIZONTAL, 20f)
                      .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  fun getOptions(componentContext: ComponentContext): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_export_markdown,
        subtitle = R.string.home_option_export_markdown_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = { sBackupMarkdown = !sBackupMarkdown },
        isSelectable = true,
        selected = sBackupMarkdown
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = { sBackupLockedNotes = !sBackupLockedNotes },
        isSelectable = true,
        selected = sBackupLockedNotes
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            sAutoBackupMode -> {
              sAutoBackupMode = false
            }
            hasAllPermissions -> {
              sAutoBackupMode = true
            }
            else -> openSheet(activity, PermissionBottomSheet())
          }
        },
        isSelectable = true,
        selected = sAutoBackupMode
      ))
    return options
  }

}