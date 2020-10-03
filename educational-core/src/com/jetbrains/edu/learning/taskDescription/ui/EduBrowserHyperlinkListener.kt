package com.jetbrains.edu.learning.taskDescription.ui

import com.intellij.ui.BrowserHyperlinkListener
import com.jetbrains.edu.learning.EduBrowser
import javax.swing.event.HyperlinkEvent

open class EduBrowserHyperlinkListener : BrowserHyperlinkListener() {
  override fun hyperlinkActivated(e: HyperlinkEvent) {
    super.hyperlinkActivated(e)

    val host = e.url?.toString() ?: return
    EduBrowser.countUsage(host)
  }

  companion object {
    @JvmField val INSTANCE: EduBrowserHyperlinkListener = EduBrowserHyperlinkListener()
  }
}