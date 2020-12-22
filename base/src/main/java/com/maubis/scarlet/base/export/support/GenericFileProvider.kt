package com.maubis.scarlet.base.export.support

import androidx.core.content.FileProvider

class GenericFileProvider : FileProvider() {
  companion object {
    var PROVIDER = "com.maubis.scarlet.base.export.support.GenericFileProvider"
  }
}
