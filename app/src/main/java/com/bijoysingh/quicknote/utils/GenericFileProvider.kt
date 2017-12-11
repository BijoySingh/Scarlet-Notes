package com.bijoysingh.quicknote.utils

import android.support.v4.content.FileProvider

class GenericFileProvider : FileProvider() {
  companion object {
    var PROVIDER = "com.bijoysingh.quicknote.utils.GenericFileProvider"
  }
}
