package com.bijoysingh.quicknote.items

class SimpleOptionsItem(
    val title: Int,
    val selected: Boolean = false,
    val listener: () -> Unit)