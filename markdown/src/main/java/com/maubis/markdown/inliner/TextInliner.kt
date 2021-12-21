package com.maubis.markdown.inliner

import com.maubis.markdown.MarkdownConfig.Companion.config

class TextInliner(val text: String) {

  val inlineConfig = config.inlinerConfig

  private var textSegment = NormalInlineBuilder()
  private var currentInline = MarkdownInlineBuilder()
  private val processedSegments = ArrayList<MarkdownInlineBuilder>()

  fun get(): MarkdownInline {
    return processSegments()
  }

  private fun processSegments(): MarkdownInline {
    processedSegments.clear()
    processedSegments.add(MarkdownInlineBuilder())

    val allConfigurations = ArrayList<IInlineConfig>()
    inlineConfig.configuration.forEach {
      if (it.type() !== MarkdownInlineType.INVALID && it.type() !== MarkdownInlineType.NORMAL) {
        allConfigurations.add(it)
      }
    }
    allConfigurations.sortByDescending { it.startIncrement() }

    var index = 0
    while (index < text.length) {
      val char = text.get(index)

      if (currentInline.config.type() == MarkdownInlineType.INLINE_CODE
          && !currentInline.config.isEnd(text, index)) {
        textSegment.builder.append(char)
        index += 1
        continue
      }

      if (currentInline.config.type() == MarkdownInlineType.IGNORE_CHAR) {
        textSegment.builder.append(char)
        addTextComponent()
        currentInline.paired = true
        index += 1
        unshelveSegment()
        continue
      }

      if (currentInline.config.type() != MarkdownInlineType.INVALID
          && currentInline.config.isEnd(text, index)) {
        addTextComponent()
        currentInline.paired = true
        index += currentInline.config.endIncrement()
        unshelveSegment()
        continue
      }

      val match = allConfigurations.firstOrNull { it.isStart(text, index) }
      if (match !== null) {
        addTextComponent()
        shelveSegment()
        currentInline.config = match
        index += match.startIncrement()
        continue
      }

      textSegment.builder.append(char)
      index += 1
    }
    addTextComponent()
    shelveSegment()
    debug()

    // Now we can have multiple unfinished left if the user did something stupid ;)
    allConfigurations.forEach {
      while (pairPoorlyPairedConfigs(it)) {
      }
    }
    pairInvalids()

    val result = removeInvalids(currentInline.build())
    if (result is PhraseDelimiterMarkdownInline && result.children.size == 1) {
      return result.children.first()
    }
    return result
  }

  /**
   * It can be something is not paired because of a user fault like "** something `something **"
   * the ` in the middle will fuck it up
   */
  private fun pairPoorlyPairedConfigs(config: IInlineConfig): Boolean {
    val count = processedSegments.count { it.config.identifier() == config.identifier() }
    if (count <= 1) {
      return false
    }

    var state = 0
    val before = ArrayList<MarkdownInlineBuilder>()
    val between = ArrayList<MarkdownInlineBuilder>()
    val after = ArrayList<MarkdownInlineBuilder>()
    for (segment in processedSegments) {
      if (segment.config.identifier() == config.identifier() && state == 0) {
        state = 1
        segment.config = InvalidInline(MarkdownInlineType.INVALID)
        between.add(segment)
        continue
      }

      if (segment.config.identifier() == config.identifier() && state == 1) {
        segment.config = InvalidInline(MarkdownInlineType.INVALID)
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
    current.config = config
    current.paired = true
    between.forEach {
      val inlineConfig = it.config
      if (inlineConfig is PhraseDelimiterInline) {
        current.children.add(NormalInlineMarkdownSegment(inlineConfig.startDelimiter))
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

  /**
   * After the pairingPoorly, it is possible to be stuck in a situation where you simply didnt pair.
   * Like "something ** something"
   */
  private fun pairInvalids() {
    currentInline = MarkdownInlineBuilder()
    processedSegments.forEach {
      val inlineConfig = it.config
      if (inlineConfig is PhraseDelimiterInline && !it.paired) {
        it.children.add(0, NormalInlineMarkdownSegment(inlineConfig.startDelimiter))
      }

      if (!it.paired || it.config.type() == MarkdownInlineType.INVALID) {
        currentInline.children.addAll(it.children)
      } else {
        currentInline.children.add(it.build())
      }
    }
    processedSegments.clear()
  }

  /**
   * We are still not done... It might be that end up with INVALIDs inside recursively
   */
  private fun removeInvalids(markdown: MarkdownInline): MarkdownInline {
    if (markdown !is PhraseDelimiterMarkdownInline) {
      return markdown
    }

    val builder = MarkdownInlineBuilder()
    builder.config = markdown.config()
    markdown.children.forEach {
      val child = removeInvalids(it)
      when {
        child.type() == MarkdownInlineType.INVALID
            && child is PhraseDelimiterMarkdownInline -> builder.children.addAll(child.children)
        else -> builder.children.add(child)
      }
    }
    return builder.build()
  }

  private fun addTextComponent() {
    val segment = textSegment.build()
    if (segment.text == "") {
      return
    }
    currentInline.children.add(segment)
    textSegment = NormalInlineBuilder()
  }

  private fun unshelveSegment() {
    processedSegments.last().children.add(currentInline.build())
    currentInline = processedSegments.last()
    processedSegments.removeAt(processedSegments.size - 1)
  }

  private fun shelveSegment() {
    if (currentInline.config.type() == MarkdownInlineType.INVALID && currentInline.children.isEmpty()) {
      currentInline = MarkdownInlineBuilder()
      return
    }

    processedSegments.add(currentInline)
    currentInline = MarkdownInlineBuilder()
  }

  private fun debug() {
    val text = StringBuilder()
    processedSegments.forEach {
      text.append(it.build().debug())
      text.append(", ")
    }
  }
}