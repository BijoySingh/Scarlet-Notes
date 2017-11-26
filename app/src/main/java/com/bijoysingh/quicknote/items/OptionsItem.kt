package com.bijoysingh.quicknote.items

import android.view.View

class OptionsItem(
    val title: Int,
    val subtitle: Int,
    val icon: Int,
    val selected: Boolean = false,
    val listener: View.OnClickListener)