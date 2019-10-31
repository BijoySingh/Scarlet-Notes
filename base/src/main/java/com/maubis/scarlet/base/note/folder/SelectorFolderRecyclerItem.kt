package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.support.v4.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.ThemeColorType

class SelectorFolderRecyclerItem(context: Context, val folder: Folder) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColored(folder.color)
  val title = folder.title
  val titleColor = sAppTheme.get(ThemeColorType.TERTIARY_TEXT)

  val folderColor = folder.color
  val iconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }
  override val type = Type.FOLDER
}
