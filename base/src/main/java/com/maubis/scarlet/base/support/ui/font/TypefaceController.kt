package com.maubis.scarlet.base.support.ui.font

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase

const val KEY_PREFERENCE_TYPEFACE = "typeface_setting"
var sPreferenceTypeface: String
  get() = ApplicationBase.sAppPreferences.get(KEY_PREFERENCE_TYPEFACE, TypefaceController.TypefaceType.APP_DEFAULT.name)
  set(value) = ApplicationBase.sAppPreferences.put(KEY_PREFERENCE_TYPEFACE, value)

class TypefaceController(context: Context) {
  enum class TypefaceType(val title: Int, val isLiteEnabled: Boolean) {
    APP_DEFAULT(R.string.typeface_title_app_default, true),
    OS_DEFAULT(R.string.typeface_title_os_default, true),
    MONOSPACE(R.string.typeface_title_monospace, false),
    SERIF_TITLE(R.string.typeface_title_serif, false),
  }

  data class TypefaceSet(
    val heading: Typeface = Typeface.DEFAULT,
    val subHeading: Typeface = Typeface.DEFAULT,
    val title: Typeface = Typeface.DEFAULT,
    val text: Typeface = Typeface.DEFAULT,
    val code: Typeface = Typeface.MONOSPACE
  )

  private var mTypefaceSet: TypefaceSet = TypefaceSet()

  init {
    notifyChange(context)
  }

  fun notifyChange(context: Context) {
    mTypefaceSet = getSetForType(context, getTypefaceSetting())
    setMarkdownConfig()
  }

  fun getSetForType(context: Context, typefaceType: TypefaceType): TypefaceSet {
    return when (typefaceType) {
      TypefaceType.APP_DEFAULT -> TypefaceSet(
        heading = ResourcesCompat.getFont(context, R.font.monserrat_bold) ?: Typeface.DEFAULT,
        subHeading = ResourcesCompat.getFont(context, R.font.monserrat_medium) ?: Typeface.DEFAULT,
        title = ResourcesCompat.getFont(context, R.font.monserrat) ?: Typeface.DEFAULT,
        text = ResourcesCompat.getFont(context, R.font.open_sans) ?: Typeface.DEFAULT,
        code = Typeface.MONOSPACE)
      TypefaceType.OS_DEFAULT -> TypefaceSet()
      TypefaceType.MONOSPACE -> TypefaceSet(
        heading = ResourcesCompat.getFont(context, R.font.mono_bold_xml) ?: Typeface.MONOSPACE,
        subHeading = ResourcesCompat.getFont(context, R.font.mono_medium_xml) ?: Typeface.MONOSPACE,
        title = ResourcesCompat.getFont(context, R.font.mono_regular_xml) ?: Typeface.MONOSPACE,
        text = ResourcesCompat.getFont(context, R.font.mono_regular_xml) ?: Typeface.MONOSPACE,
        code = Typeface.MONOSPACE)
      TypefaceType.SERIF_TITLE -> TypefaceSet(
        heading = ResourcesCompat.getFont(context, R.font.serif_bold_xml) ?: Typeface.SERIF,
        subHeading = ResourcesCompat.getFont(context, R.font.serif_bold_xml) ?: Typeface.SERIF,
        title = ResourcesCompat.getFont(context, R.font.serif_regular_xml) ?: Typeface.SERIF,
        text = ResourcesCompat.getFont(context, R.font.open_sans) ?: Typeface.DEFAULT,
        code = Typeface.MONOSPACE)
    }
  }

  private fun setMarkdownConfig() {
    MarkdownConfig.config.spanConfig.headingTypeface = subHeading()
    MarkdownConfig.config.spanConfig.heading2Typeface = title()
    MarkdownConfig.config.spanConfig.heading3Typeface = title()
    MarkdownConfig.config.spanConfig.textTypeface = text()
    MarkdownConfig.config.spanConfig.codeTypeface = code()
  }

  private fun getTypefaceSetting(): TypefaceType {
    return try {
      TypefaceType.valueOf(sPreferenceTypeface)
    } catch (exception: Exception) {
      TypefaceType.APP_DEFAULT
    }
  }

  fun heading(): Typeface = mTypefaceSet.heading

  fun subHeading(): Typeface = mTypefaceSet.subHeading

  fun title(): Typeface = mTypefaceSet.title

  fun text(): Typeface = mTypefaceSet.text

  fun code(): Typeface = mTypefaceSet.code
}