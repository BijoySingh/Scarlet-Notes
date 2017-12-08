package com.bijoysingh.quicknote.items

import android.view.View

class OptionsItem(
    val title: Int,
    val subtitle: Int,
    val icon: Int,
    val selected: Boolean = false, // indicates its a selected option (blue color)
    val visible: Boolean = true, // indicates if the option is visible
    val enabled: Boolean = false, // indicates if the option will show a checked on the side
    val listener: View.OnClickListener)