@file:JvmName("RemoteEduCourseMixins")

package com.jetbrains.edu.coursecreator.actions.mixins

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.jetbrains.edu.coursecreator.actions.mixins.JsonMixinNames.ID
import com.jetbrains.edu.coursecreator.actions.mixins.JsonMixinNames.STEPIK_ID
import com.jetbrains.edu.coursecreator.actions.mixins.JsonMixinNames.UNIT_ID
import com.jetbrains.edu.coursecreator.actions.mixins.JsonMixinNames.UPDATE_DATE
import java.util.*

@Suppress("unused", "UNUSED_PARAMETER") // used for json serialization
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonPropertyOrder(ID, UPDATE_DATE)
abstract class RemoteEduCourseMixin : LocalEduCourseMixin() {
  @JsonProperty(ID)
  private var myId: Int = 0

  @JsonProperty(UPDATE_DATE)
  private lateinit var myUpdateDate: Date
}

@Suppress("UNUSED_PARAMETER", "unused") // used for json serialization
abstract class RemoteSectionMixin : LocalSectionMixin() {
  @JsonProperty(ID)
  private var myId: Int = 0

  @JsonProperty(UPDATE_DATE)
  private lateinit var myUpdateDate: Date

}

@Suppress("UNUSED_PARAMETER", "unused") // used for json serialization
abstract class RemoteLessonMixin : LocalLessonMixin() {
  @JsonProperty(ID)
  private var myId: Int = 0

  @JsonProperty(UNIT_ID)
  private var unitId: Int = 0

  @JsonProperty(UPDATE_DATE)
  private lateinit var myUpdateDate: Date
}

@Suppress("UNUSED_PARAMETER", "unused") // used for json serialization
abstract class RemoteTaskMixin : LocalTaskMixin() {
  @JsonProperty(STEPIK_ID)
  private var myId: Int = 0

  @JsonProperty(UPDATE_DATE)
  private lateinit var myUpdateDate: Date

}