package com.bijoysingh.quicknote.items

import java.io.File

class FileRecyclerItem(val name: String,
                       val date: Long,
                       val path: String,
                       val file: File): RecyclerItem() {

  override fun getType(): Type {
    return Type.FILE
  }

}