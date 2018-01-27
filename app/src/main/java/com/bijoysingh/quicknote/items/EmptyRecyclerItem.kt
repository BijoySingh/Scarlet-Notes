package com.bijoysingh.quicknote.items

class EmptyRecyclerItem : RecyclerItem() {
  override fun getType(): RecyclerItem.Type = RecyclerItem.Type.EMPTY
}
