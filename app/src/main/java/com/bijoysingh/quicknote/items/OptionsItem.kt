package com.bijoysingh.quicknote.items

import android.view.View

class OptionsItem(
    val title: Int,
    val subtitle: Int,
    val icon: Int,
    val selected: Boolean = false, // indicates its a selected option (blue color)
    val visible: Boolean = true, // indicates if the option is visible
    val enabled: Boolean = false, // indicates if the option will show a checked on the side
    val invalid: Boolean = false, // indicates that the option will be faded and click removed
    val listener: View.OnClickListener)