package com.maubis.scarlet.base.export.support

import android.support.v4.content.FileProvider

class GenericFileProvider : FileProvider() {
  companion object {
    var PROVIDER = "com.maubis.scarlet.base.export.support.GenericFileProvider"
  }
}
