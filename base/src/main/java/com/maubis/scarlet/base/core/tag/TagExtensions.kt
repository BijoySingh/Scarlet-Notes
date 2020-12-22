package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.database.room.tag.Tag

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}
