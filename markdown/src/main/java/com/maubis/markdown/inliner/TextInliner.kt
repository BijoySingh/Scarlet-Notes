package com.maubis.markdown.inliner

import android.util.Log

class TextInliner(val text: String) {

  object Delimiters {
    const val BOLD = "**"
    const val ITALICS = '_'
    const val UNDERLINE = '*'
    const val STRIKE = '~'
    const val INLINE_CODE = '`'
  }

  private var textSegment = NormalInlineBuilder()
  private var currentSegment = MarkdownInlineBuilder()
  private val processedSegments = ArrayList<MarkdownInlineBuilder>()

  fun get(): MarkdownInline {
    processSegments()
    val result = removeInvalids(currentSegment.build())
    if (result is ComplexMarkdownInline && result.children.size == 1) {
      return result.children.first()
    }
    return result
  }

  private fun processSegments() {
    processedSegments.clear()
    processedSegments.add(MarkdownInlineBuilder())

    val textToProcess = text
        .replace("<i>", "_")
        .replace("</i>", "_")
        .replace("<u>", "*")
        .replace("</u>", "*")

    var index = 0
    while (index < textToProcess.length) {
      val char = textToProcess.get(index)
      val nextChar = if (index + 1 < textToProcess.length) textToProcess.get(index + 1) else null

      if (char != Delimiters.INLINE_CODE && currentSegment.markdownType == MarkdownInlineType.INLINE_CODE) {
        textSegment.builder.append(char)
        index += 1
        continue
      }

      if (char == Delimiters.UNDERLINE
          && textSegment.builder.isEmpty()
          && (currentSegment.markdownType == MarkdownInlineType.UNDERLINE)
          && currentSegment.children.isEmpty()) {
        currentSegment.markdownType = MarkdownInlineType.BOLD
        index += 1
        continue
      }

      if ((char == Delimiters.UNDERLINE && currentSegment.markdownType == MarkdownInlineType.UNDERLINE)
          || (char == Delimiters.ITALICS && currentSegment.markdownType == MarkdownInlineType.ITALICS)
          || (char == Delimiters.INLINE_CODE && currentSegment.markdownType == MarkdownInlineType.INLINE_CODE)
          || (char == Delimiters.STRIKE && currentSegment.markdownType == MarkdownInlineType.STRIKE)) {
        addTextComponent()
        currentSegment.paired = true
        unshelveSegment()
        index += 1
        continue
      }

      if (char == Delimiters.UNDERLINE && nextChar == Delimiters.UNDERLINE && currentSegment.markdownType == MarkdownInlineType.BOLD) {
        addTextComponent()
        unshelveSegment()
        index += 2
        continue
      }

      if (char == Delimiters.UNDERLINE || char == Delimiters.ITALICS || char == Delimiters.INLINE_CODE || char == Delimiters.STRIKE) {
        addTextComponent()
        shelveSegment()
        currentSegment.markdownType = when (char) {
          Delimiters.UNDERLINE -> MarkdownInlineType.UNDERLINE
          Delimiters.ITALICS -> MarkdownInlineType.ITALICS
          Delimiters.INLINE_CODE -> MarkdownInlineType.INLINE_CODE
          Delimiters.STRIKE -> MarkdownInlineType.STRIKE
          else -> MarkdownInlineType.INVALID
        }
        index += 1
        continue
      }

      textSegment.builder.append(char)
      index += 1
    }
    addTextComponent()
    shelveSegment()
    debug()

    // Now we can have multiple unfinished left if the user did something stupid ;)
    while (dedup(MarkdownInlineType.BOLD)) {
    }
    while (dedup(MarkdownInlineType.ITALICS)) {
    }
    while (dedup(MarkdownInlineType.UNDERLINE)) {
    }
    while (dedup(MarkdownInlineType.STRIKE)) {
    }
    while (dedup(MarkdownInlineType.INLINE_CODE)) {
    }
    dedupInvalids()
  }

  private fun dedupInvalids() {
    debug()
    currentSegment = MarkdownInlineBuilder()
    processedSegments.forEach {
      val inline = getSegmentForType(it.markdownType)
      if (inline is NormalInlineSegment && !it.paired) {
        it.children.add(inline)
      }
      if (!it.paired || it.markdownType == MarkdownInlineType.INVALID) {
        currentSegment.children.addAll(it.children)
      } else {
        currentSegment.children.add(it.build())
      }
    }
    Log.d("Inliner", currentSegment.build().debug())
    processedSegments.clear()
  }

  private fun getSegmentForType(type: MarkdownInlineType): MarkdownInline {
    return when (type) {
      MarkdownInlineType.INLINE_CODE -> NormalInlineSegment("`")
      MarkdownInlineType.BOLD -> NormalInlineSegment("**")
      MarkdownInlineType.ITALICS -> NormalInlineSegment("_")
      MarkdownInlineType.UNDERLINE -> NormalInlineSegment("*")
      MarkdownInlineType.STRIKE -> NormalInlineSegment("~")
      else -> return DefaultMarkdownInline(emptyList())
    }
  }

  private fun removeInvalids(markdown: MarkdownInline): MarkdownInline {
    if (markdown !is ComplexMarkdownInline) {
      return markdown
    }

    val builder = MarkdownInlineBuilder()
    builder.markdownType = markdown.type()
    markdown.children.forEach {
      val child = removeInvalids(it)
      if (child.type() == MarkdownInlineType.INVALID && child is ComplexMarkdownInline) {
        builder.children.addAll(child.children)
      } else {
        builder.children.add(child)
      }
    }
    return builder.build()
  }

  private fun dedup(type: MarkdownInlineType): Boolean {
    val count = processedSegments.count { it.markdownType == type }
    if (count <= 1) {
      return false
    }

    debug()

    var state = 0
    val before = ArrayList<MarkdownInlineBuilder>()
    val between = ArrayList<MarkdownInlineBuilder>()
    val after = ArrayList<MarkdownInlineBuilder>()
    for (segment in processedSegments) {
      if (segment.markdownType == type && state == 0) {
        state = 1
        segment.markdownType = MarkdownInlineType.INVALID
        between.add(segment)
        continue
      }

      if (segment.markdownType == type && state == 1) {
        segment.markdownType = MarkdownInlineType.INVALID
        after.add(segment)
        state = 2
        continue
      }

      when {
        (state == 0) -> before.add(segment)
        (state == 1) -> between.add(segment)
        (state == 2) -> after.add(segment)
      }
    }

    val current = MarkdownInlineBuilder()
    current.markdownType = type
    current.paired = true
    between.forEach {
      val inline = getSegmentForType(it.markdownType)
      if (inline is NormalInlineSegment) {
        current.children.add(inline)
      }
      current.children.addAll(it.children)
    }

    processedSegments.clear()
    processedSegments.addAll(before)
    processedSegments.add(current)
    processedSegments.addAll(after)

    debug()
    return true
  }

  private fun addTextComponent() {
    val segment = textSegment.build()
    if (segment.text == "") {
      return
    }
    currentSegment.children.add(segment)
    textSegment = NormalInlineBuilder()
  }

  private fun unshelveSegment() {
    processedSegments.last().children.add(currentSegment.build())
    currentSegment = processedSegments.last()
    processedSegments.removeAt(processedSegments.size - 1)
  }

  private fun shelveSegment() {
    if (currentSegment.markdownType == MarkdownInlineType.INVALID && currentSegment.children.isEmpty()) {
      currentSegment = MarkdownInlineBuilder()
      return
    }

    processedSegments.add(currentSegment)
    currentSegment = MarkdownInlineBuilder()
  }

  private fun debug() {
    val text = StringBuilder()
    processedSegments.forEach {
      text.append(it.build().debug())
      text.append(", ")
    }
    Log.d("Inliner", "processed: ${text.toString()}")
  }
}