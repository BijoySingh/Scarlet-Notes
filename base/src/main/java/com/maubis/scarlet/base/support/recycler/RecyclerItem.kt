package com.maubis.scarlet.base.support.recycler

abstract class RecyclerItem {

  abstract val type: Type

  enum class Type {
    NOTE,
    EMPTY,
    FILE,
    FOLDER,
    INFORMATION
  }
}
