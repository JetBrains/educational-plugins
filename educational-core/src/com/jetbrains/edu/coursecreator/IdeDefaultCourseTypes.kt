package com.jetbrains.edu.coursecreator

import com.intellij.lang.Language
import com.intellij.util.PlatformUtils
import com.jetbrains.edu.coursecreator.ui.CCNewCoursePanel
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.EduUtils

fun getDefaultCourseType(courses: List<CCNewCoursePanel.CourseTypeData>) = courses.find {
  it.language == Language.findLanguageByID(getDefaultLanguageId())
}

fun getDefaultLanguageId() =
  when (true) {
    (PlatformUtils.isIntelliJ() || EduUtils.isAndroidStudio()) -> EduNames.KOTLIN
    (PlatformUtils.isPyCharm()) -> EduNames.PYTHON
    (PlatformUtils.isWebStorm()) -> EduNames.JAVASCRIPT
    (PlatformUtils.isCLion()) -> EduNames.RUST
    else -> null
  }

