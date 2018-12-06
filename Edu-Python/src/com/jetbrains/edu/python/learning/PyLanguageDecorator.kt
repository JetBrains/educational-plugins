package com.jetbrains.edu.python.learning

import com.jetbrains.edu.learning.EduLanguageDecorator

open class PyLanguageDecorator : EduLanguageDecorator {
  override fun getLanguageScriptUrl(): String = javaClass.classLoader.getResource("/code-mirror/python.js").toExternalForm()
  override fun getDefaultHighlightingMode(): String = "python"
}