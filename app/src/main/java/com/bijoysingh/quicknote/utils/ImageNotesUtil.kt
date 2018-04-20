package com.bijoysingh.quicknote.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.maubis.scarlet.base.note.NoteImage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File

fun loadFileToImageView(context: Context, image: ImageView, file: File, callback: Callback? = null) {
  Picasso.with(context).load(file).into(image, object : Callback {
    override fun onSuccess() {
      // Ignore successful call
      image.visibility = View.VISIBLE
      callback?.onSuccess()
    }

    override fun onError() {
      NoteImage(context).deleteIfExist(file)
      image.visibility = View.GONE
      callback?.onError()
    }
  })
}