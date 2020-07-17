package com.jetbrains.edu.javascript.learning.stepik.hyperskill

import com.jetbrains.edu.javascript.learning.JsConfigurator
import com.jetbrains.edu.javascript.learning.JsNewProjectSettings
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.stepik.hyperskill.HyperskillConfigurator

class JsHyperskillConfigurator : HyperskillConfigurator<JsNewProjectSettings>(object : JsConfigurator() {
  override fun getMockFileName(text: String): String = MAIN_JS
}) {
  override val testDirs: List<String>
    get() = listOf(HYPERSKILL_TEST_DIR, EduNames.TEST)
}
