package com.jetbrains.edu.learning.stepik.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.module.SimpleModule
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.edu.learning.EduSettings
import com.jetbrains.edu.learning.courseFormat.CourseCompatibility
import com.jetbrains.edu.learning.courseFormat.CourseVisibility
import com.jetbrains.edu.learning.courseFormat.EduCourse
import com.jetbrains.edu.learning.stepik.StepikConnector.FEATURED_COURSES
import com.jetbrains.edu.learning.stepik.StepikNames
import com.jetbrains.edu.learning.stepik.StepikUser
import com.jetbrains.edu.learning.stepik.StepikUserInfo
import com.jetbrains.edu.learning.stepik.StepikUtils
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.apache.http.HttpStatus
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object StepikNewConnector {
  private val LOG = Logger.getInstance(StepikNewConnector::class.java)
  private val converterFactory: JacksonConverterFactory

  init {
    val module = SimpleModule()
    val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    objectMapper.addMixIn(EduCourse::class.java, StepikEduCourseMixin::class.java)
    objectMapper.registerModule(module)
    converterFactory = JacksonConverterFactory.create(objectMapper)
  }

  private val authorizationService: StepikOAuthService
    get() {
      val retrofit = Retrofit.Builder()
        .baseUrl(StepikNames.STEPIK_URL)
        .addConverterFactory(converterFactory)
        .build()

      return retrofit.create(StepikOAuthService::class.java)
    }

  private val service: StepikService
    get() = service(EduSettings.getInstance().user)

  private fun service(account: StepikUser?): StepikService {
    if (account != null && !account.tokenInfo.isUpToDate()) {
      account.refreshTokens()
    }

    val dispatcher = Dispatcher()
    dispatcher.maxRequests = 10

    val okHttpClient = OkHttpClient.Builder()
      .readTimeout(60, TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addInterceptor { chain ->
        val tokenInfo = account?.tokenInfo
        if (tokenInfo == null) return@addInterceptor chain.proceed(chain.request())

        val newRequest = chain.request().newBuilder()
          .addHeader("Authorization", "Bearer ${tokenInfo.accessToken}")
          .build()
        chain.proceed(newRequest)
      }
      .dispatcher(dispatcher)
      .build()

    val retrofit = Retrofit.Builder()
      .baseUrl(StepikNames.STEPIK_API_URL_SLASH)
      .addConverterFactory(converterFactory)
      .client(okHttpClient)
      .build()

    return retrofit.create(StepikService::class.java)
  }

  private fun StepikUser.refreshTokens() {
    val refreshToken = tokenInfo.refreshToken
    val tokens = authorizationService.refreshTokens("refresh_token", StepikNames.CLIENT_ID, refreshToken).execute().body()
    if (tokens != null) {
      updateTokens(tokens)
    }
  }

  private fun getCurrentUserInfo(stepikUser: StepikUser): StepikUserInfo? {
    return service(stepikUser).getCurrentUser().execute().body()?.users?.firstOrNull() ?: return null
  }

  fun login(code: String, redirectUri: String): Boolean {
    val tokenInfo = authorizationService.getTokens(StepikNames.CLIENT_ID, redirectUri,
                                                   code, "authorization_code").execute().body() ?: return false
    val stepikUser = StepikUser(tokenInfo)
    val stepikUserInfo = getCurrentUserInfo(stepikUser) ?: return false
    stepikUser.userInfo = stepikUserInfo
    EduSettings.getInstance().user = stepikUser
    return true
  }

  fun isEnrolledToCourse(courseId: Int, stepikUser: StepikUser): Boolean {
    val response = service(stepikUser).enrollments(courseId).execute()
    return response.code() == HttpStatus.SC_OK
  }

  fun enrollToCourse(courseId: Int, stepikUser: StepikUser) {
    val response = service(stepikUser).enrollments(EnrollmentData(courseId)).execute()
    if (response.code() != HttpStatus.SC_CREATED) {
      LOG.error("Failed to enroll user ${stepikUser.id} to course $courseId")
    }
  }

  fun getCourseInfos(isPublic: Boolean): List<EduCourse> {
    val result = mutableListOf<EduCourse>()
    var currentPage = 1
    val enrolled = if (isPublic) null else true
    val indicator = ProgressManager.getInstance().progressIndicator
    while (true) {
      if (indicator != null && indicator.isCanceled) break
      val coursesList = service.courses(true, isPublic, currentPage, enrolled).execute().body()
      if (coursesList == null) break

      for (info in coursesList.courses) {
        StepikUtils.setCourseLanguage(info)
      }
      addAvailableCourses(result, coursesList)
      currentPage += 1
      if (!coursesList.meta.containsKey("has_next") || coursesList.meta["has_next"] == false) break
    }
    return result
  }

  private fun addAvailableCourses(result: MutableList<EduCourse>, coursesList: CoursesList) {
    val courses = coursesList.courses
    for (info in courses) {
      if (StringUtil.isEmptyOrSpaces(info.type)) continue
      if (info.compatibility === CourseCompatibility.UNSUPPORTED) continue
      info.visibility = getVisibility(info)
      result.add(info)
    }
  }

  private fun getVisibility(course: EduCourse): CourseVisibility {
    return when {
      !course.isPublic -> CourseVisibility.PrivateVisibility
      FEATURED_COURSES.contains(course.id) -> CourseVisibility.FeaturedVisibility(FEATURED_COURSES.indexOf(course.id))
      FEATURED_COURSES.isEmpty() -> CourseVisibility.LocalVisibility
      else -> CourseVisibility.PublicVisibility
    }
  }

}
