package com.maubis.scarlet.base.main.recycler

import com.maubis.scarlet.base.support.recycler.RecyclerItem

class EmptyFolderRecyclerItem : RecyclerItem() {
  override val type = RecyclerItem.Type.EMPTY
}

class GenericRecyclerItem(itemType: Type) : RecyclerItem() {
  override val type = itemType
}
