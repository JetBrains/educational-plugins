package com.jetbrains.edu.android

import com.jetbrains.edu.jvm.gradle.GradleConfiguratorBase
import com.jetbrains.edu.jvm.gradle.GradleCourseBuilderBase
import com.jetbrains.edu.learning.EduExperimentalFeatures
import com.jetbrains.edu.learning.checker.TaskCheckerProvider
import com.jetbrains.edu.learning.isFeatureEnabled
import com.jetbrains.edu.learning.isUnitTestMode
import icons.AndroidIcons
import javax.swing.Icon

class AndroidConfigurator : GradleConfiguratorBase() {
  override val courseBuilder: GradleCourseBuilderBase
    get() = AndroidCourseBuilder()

  override val taskCheckerProvider: TaskCheckerProvider
    get() = AndroidTaskCheckerProvider()

  override val sourceDir: String
    get() = "src/main"

  override val testDirs: List<String>
    get() = listOf("src/test", "src/androidTest")

  override val testFileName: String
    get() = "ExampleUnitTest.kt"

  override val isCourseCreatorEnabled: Boolean
    get() = isFeatureEnabled(EduExperimentalFeatures.ANDROID_COURSES) || isUnitTestMode

  override fun getMockFileName(text: String): String = "Main.kt"

  override val logo: Icon
    get() = AndroidIcons.Android
}
