package com.jetbrains.edu.learning.stepik.submissions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.courseFormat.ext.getVirtualFile
import com.jetbrains.edu.learning.courseFormat.ext.isTestFile
import com.jetbrains.edu.learning.courseFormat.tasks.Task
import com.jetbrains.edu.learning.courseFormat.tasks.choice.ChoiceTask
import com.jetbrains.edu.learning.document
import com.jetbrains.edu.learning.messages.EduCoreBundle
import com.jetbrains.edu.learning.stepik.StepikNames
import com.jetbrains.edu.learning.stepik.StepikSolutionsLoader
import com.jetbrains.edu.learning.stepik.api.Reply
import com.jetbrains.edu.learning.stepik.api.SolutionFile
import com.jetbrains.edu.learning.stepik.api.Submission
import com.jetbrains.edu.learning.taskDescription.ui.SwingToolWindowLinkHandler
import com.jetbrains.edu.learning.taskDescription.ui.styleManagers.StyleManager
import com.jetbrains.edu.learning.taskDescription.ui.styleManagers.StyleResourcesManager
import com.jetbrains.edu.learning.taskDescription.ui.styleManagers.TaskDescriptionBundle
import com.jetbrains.edu.learning.taskDescription.ui.tab.AdditionalTab
import com.jetbrains.edu.learning.taskDescription.ui.tab.SwingTextPanel
import com.jetbrains.edu.learning.taskDescription.ui.tab.TabManager.TabType.SUBMISSIONS_TAB
import com.jetbrains.edu.learning.taskDescription.ui.tab.TabTextPanel
import com.jetbrains.edu.learning.ui.EduColors
import icons.EducationalCoreIcons
import java.net.URL
import java.text.DateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.math.roundToInt

class SubmissionsTab private constructor(
  project: Project,
  private val linkHandler: SwingToolWindowLinkHandler?,
  description: String
) : AdditionalTab(project, SUBMISSIONS_TAB) {

  init {
    init()
    setText(description, plain = true)
  }

  override fun createTextPanel(): TabTextPanel = SwingTextPanel(project, linkHandler)

  companion object {
    private const val SUBMISSION_PROTOCOL = "submission://"
    private const val SUBMISSION_DIFF_URL = "${SUBMISSION_PROTOCOL}diff/"
    private const val SUBMISSION_LOGIN_URL = "${SUBMISSION_PROTOCOL}login/"

    private val textStyleHeader: String
      get() = StyleManager().textStyleHeader

    fun create(project: Project, task: Task): SubmissionsTab {
      val submissionsManager = SubmissionsManager.getInstance(project)
      val descriptionText = StringBuilder()
      var linkHandler: SwingToolWindowLinkHandler? = null

      if (submissionsManager.isLoggedIn()) {
        val submissionsList = submissionsManager.getSubmissionsFromMemory(setOf(task.id))
        if (submissionsList != null) {
          when {
            task is ChoiceTask -> descriptionText.addViewOnStepikLink(task)
            submissionsList.isEmpty() -> descriptionText.addEmptySubmissionsMessage()
            else -> {
              descriptionText.addSubmissions(submissionsList)
              linkHandler = SubmissionsDifferenceLinkHandler(project, task, submissionsManager)
            }
          }
        }
        else {
          descriptionText.addEmptySubmissionsMessage()
        }
      }
      else {
        descriptionText.addLoginText(submissionsManager)
        linkHandler = LoginLinkHandler(project, submissionsManager)
      }

      return SubmissionsTab(project, linkHandler, descriptionText.toString())
    }

    private class LoginLinkHandler(
      project: Project,
      private val submissionsManager: SubmissionsManager
    ) : SwingToolWindowLinkHandler(project) {
      override fun process(url: String): Boolean {
        if (!url.startsWith(SUBMISSION_LOGIN_URL)) return false

        submissionsManager.doAuthorize()
        return true
      }
    }

    private class SubmissionsDifferenceLinkHandler(
      project: Project,
      private val task: Task,
      private val submissionsManager: SubmissionsManager
    ) : SwingToolWindowLinkHandler(project) {
      override fun process(url: String): Boolean {
        if (!url.startsWith(SUBMISSION_DIFF_URL)) return false

        val submissionId = url.substringAfter(SUBMISSION_DIFF_URL).toInt()
        val submission = submissionsManager.getSubmission(task.id, submissionId) ?: return true
        val reply = submission.reply ?: return true
        runInEdt {
          showDiff(project, task, reply)
        }
        return true
      }
    }

    private fun StringBuilder.addViewOnStepikLink(task: ChoiceTask) {
      append(
        "<a ${textStyleHeader};color:#${ColorUtil.toHex(EduColors.hyperlinkColor)} " +
        "href=https://stepik.org/submissions/${task.id}?unit=${task.lesson.unitId}\">" +
        EduCoreBundle.message("submissions.view.quiz.on.stepik", StepikNames.STEPIK, "</a><a ${textStyleHeader}>") + "</a>")
    }

    private fun StringBuilder.addEmptySubmissionsMessage() {
      append("<a $textStyleHeader>${EduCoreBundle.message("submissions.empty")}")
    }

    private fun StringBuilder.addSubmissions(submissionsNext: List<Submission>) {
      append("<ul style=list-style-type:none;margin:0;padding:0;>")
      submissionsNext.forEach { submission ->
        append(submissionLink(submission))
      }
      append("</ul>")
    }

    private fun StringBuilder.addLoginText(submissionsManager: SubmissionsManager) {
      append("<a ${textStyleHeader};color:#${ColorUtil.toHex(EduColors.hyperlinkColor)} href=${SUBMISSION_LOGIN_URL}>" +
                             EduCoreBundle.message("submissions.login", submissionsManager.getPlatformName()) + "</a>")
    }

    private fun showDiff(project: Project, task: Task, reply: Reply) {
      val taskFiles = task.taskFiles.values.toMutableList()
      val submissionTexts = getSubmissionTexts(reply, task.name) ?: return
      val submissionTaskFiles = taskFiles.filter { it.isVisible && !it.isTestFile }
      val requests = submissionTaskFiles.mapNotNull {
        val virtualFile = it.getVirtualFile(project) ?: error("VirtualFile for ${it.name} not found")
        val currentFileContent = DiffContentFactory.getInstance().create(virtualFile.document.text, virtualFile.fileType)
        val submissionText = submissionTexts[it.name] ?: submissionTexts[task.name]
        if (submissionText == null) {
          null
        }
        else {
          val submissionFileContent = DiffContentFactory.getInstance().create(
            StepikSolutionsLoader.removeAllTags(submissionText),
            virtualFile.fileType)
          SimpleDiffRequest(EduCoreBundle.message("submissions.compare"),
                            currentFileContent,
                            submissionFileContent,
                            EduCoreBundle.message("submissions.local"),
                            EduCoreBundle.message("submissions.submission"))
        }
      }
      DiffManager.getInstance().showDiff(project, SimpleDiffRequestChain(requests), DiffDialogHints.FRAME)
    }

    private fun getSubmissionTexts(reply: Reply, taskName: String): Map<String, String>? {
      val solutions = reply.solution
      if (solutions == null) {
        val submissionText = reply.code ?: return null
        return mapOf(taskName to submissionText)
      }
      return solutions.stream().collect(Collectors.toMap(SolutionFile::name, SolutionFile::text))
    }

    private fun submissionLink(submission: Submission): String? {
      val time = submission.time ?: return null
      val pictureSize = (StyleManager().bodyFontSize * 0.75).roundToInt()
      val text = formatDate(time)
      return "<li><h><img src=${getImageUrl(submission.status)} hspace=6 width=${pictureSize} height=${pictureSize}/></h>" +
             "<a $textStyleHeader;color:${getLinkColor(submission)} href=${SUBMISSION_DIFF_URL}${submission.id}> ${text}</a></li>"
    }

    private fun formatDate(time: Date): String {
      val calendar = GregorianCalendar()
      calendar.time = time
      val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
      return formatter.format(calendar.time)
    }

    private fun getImageUrl(status: String?): URL? {
      val icon = when (status) {
        EduNames.CORRECT -> if (StyleResourcesManager.isHighContrast()) EducationalCoreIcons.TaskSolvedNoFrameHighContrast else EducationalCoreIcons.TaskSolvedNoFrame
        else -> if (StyleResourcesManager.isHighContrast()) EducationalCoreIcons.TaskFailedNoFrameHighContrast else EducationalCoreIcons.TaskFailedNoFrame
      }
      @Suppress("UnstableApiUsage")
      return (icon as IconLoader.CachedImageIcon).url
    }

    private fun getLinkColor(submission: Submission): String {
      return when (submission.status) {
        EduNames.CORRECT -> getCorrectLinkColor()
        else -> getWrongLinkColor()
      }
    }

    private fun getCorrectLinkColor(): String {
      return if (StyleResourcesManager.isHighContrast()) {
        TaskDescriptionBundle.value("correct.label.foreground.high.contrast")
      }
      else {
        "#${ColorUtil.toHex(EduColors.correctLabelForeground)}"
      }
    }

    private fun getWrongLinkColor(): String {
      return if (StyleResourcesManager.isHighContrast()) {
        TaskDescriptionBundle.value("wrong.label.foreground.high.contrast")
      }
      else {
        "#${ColorUtil.toHex(EduColors.wrongLabelForeground)}"
      }
    }
  }
}
