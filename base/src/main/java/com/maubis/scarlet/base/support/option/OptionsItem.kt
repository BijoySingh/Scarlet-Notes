package com.maubis.scarlet.base.support.option

import android.view.View

class OptionsItem(
  val title: Int,
  val subtitle: Int,
  val icon: Int,
  val selected: Boolean = false, // indicates its a selected option (blue color)
  val visible: Boolean = true, // indicates if the option is visible
  val enabled: Boolean = false, // indicates if the option will show a checked on the side
  val invalid: Boolean = false, // indicates that the option will be faded and click removed
  val content: String = "", // content is an alternative to subtitle when it's 0
  val actionIcon: Int = 0, // icon resource for the action
  val listener: View.OnClickListener)