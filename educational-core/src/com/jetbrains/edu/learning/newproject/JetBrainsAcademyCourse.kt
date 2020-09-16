package com.jetbrains.edu.learning.newproject

import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.compatibility.CourseCompatibilityProviderEP
import com.jetbrains.edu.learning.courseFormat.*

/**
 * Fake course type for advertising JBA
 */
class JetBrainsAcademyCourse : Course() {
  init {
    course.name = "JetBrains Academy Track"
    visibility = CourseVisibility.FeaturedVisibility(1)
    language = "NoLanguage" // to avoid accidental usage of default value - Python
    description = """
     Learn to program by creating working applications:
     
     - Choose a project and get a personal curriculum with all the concepts necessary to build it
     
     - See how it's all related with the Knowledge Map
     
     - Develop projects and solve coding tasks with professional IDEs by JetBrains
     
     JetBrains Academy experience starts in your browser 
     
     <a href="https://www.jetbrains.com/academy/?utm_source=ide&utm_content=browse-courses">Learn more</a>
   """.trimIndent()
  }

  override fun getTags(): MutableList<Tag> {
    val tags = mutableListOf<Tag>()

    tags.addAll(supportedLanguages.map { ProgrammingLanguageTag(it) })
    tags.add(HumanLanguageTag(humanLanguage))

    return tags
  }

  val supportedLanguages: List<String>
    get() = FEATURED_LANGUAGES.mapNotNull { languageId ->
      CourseCompatibilityProviderEP.find(languageId, EduNames.DEFAULT_ENVIRONMENT)?.technologyName
    }

  override fun isViewAsEducatorEnabled(): Boolean = false

  companion object {
    private val FEATURED_LANGUAGES = listOf(
      EduNames.JAVA,
      EduNames.KOTLIN,
      EduNames.PYTHON,
      EduNames.JAVASCRIPT
    )
  }
}