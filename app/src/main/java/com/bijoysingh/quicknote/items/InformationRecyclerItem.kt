package com.bijoysingh.quicknote.items

class InformationRecyclerItem(val source: Int, val function: () -> Unit) : RecyclerItem() {
  override val type = RecyclerItem.Type.INFORMATION
}
