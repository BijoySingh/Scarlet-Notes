package com.maubis.scarlet.base.note.tag.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.uibasics.R

class HomeTagView(val rootView: View) {

  val icon: ImageView
  val title: TextView
  val subtitle: TextView
  val action: ImageView

  init {
    icon = rootView.findViewById(R.id.icon)
    title = rootView.findViewById(R.id.title)
    subtitle = rootView.findViewById(R.id.subtitle)
    action = rootView.findViewById(R.id.action)
  }


}