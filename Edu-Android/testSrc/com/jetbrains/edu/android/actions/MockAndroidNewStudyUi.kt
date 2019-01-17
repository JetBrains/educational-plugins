package com.jetbrains.edu.android.actions

import com.intellij.openapi.project.Project
import com.jetbrains.edu.android.AndroidCourseBuilder
import com.jetbrains.edu.coursecreator.actions.NewStudyItemInfo
import com.jetbrains.edu.coursecreator.actions.NewStudyItemUiModel
import com.jetbrains.edu.coursecreator.actions.create.MockNewStudyItemUi
import com.jetbrains.edu.coursecreator.ui.AdditionalPanel

class MockAndroidNewStudyUi(
  name: String,
  private val packageName: String,
  index: Int? = null
) : MockNewStudyItemUi(name, index) {
  override fun showDialog(project: Project, model: NewStudyItemUiModel, additionalPanels: List<AdditionalPanel>): NewStudyItemInfo? {
    return super.showDialog(project, model, additionalPanels)?.apply {
      putUserData(AndroidCourseBuilder.PACKAGE_NAME, packageName)
      putUserData(AndroidCourseBuilder.MIN_ANDROID_SDK, 15)
      putUserData(AndroidCourseBuilder.COMPILE_ANDROID_SDK, 28)
    }
  }
}
