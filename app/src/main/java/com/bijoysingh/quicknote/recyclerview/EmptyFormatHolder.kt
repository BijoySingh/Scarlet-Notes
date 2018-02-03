package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bijoysingh.quicknote.formats.Format
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

class EmptyFormatHolder(context: Context, itemView: View) : RecyclerViewHolder<Format>(context, itemView) {

  override fun populate(data: Format, extra: Bundle) {
  }
}
