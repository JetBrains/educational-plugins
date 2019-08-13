package com.jetbrains.edu.coursecreator.yaml.format

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonAppend
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedClass
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter
import com.fasterxml.jackson.databind.util.Annotations
import com.jetbrains.edu.coursecreator.yaml.format.YamlMixinNames.CONTENT
import com.jetbrains.edu.coursecreator.yaml.format.YamlMixinNames.FRAMEWORK_TYPE
import com.jetbrains.edu.coursecreator.yaml.format.YamlMixinNames.TYPE
import com.jetbrains.edu.learning.courseFormat.FrameworkLesson
import com.jetbrains.edu.learning.courseFormat.Lesson

@JsonDeserialize(builder = FrameworkLessonBuilder::class)
@JsonAppend(props = [JsonAppend.Prop(LessonTypePropertyWriter::class, name = TYPE, type = String::class)], prepend = true)
abstract class FrameworkLessonYamlUtil : LessonYamlMixin()

@JsonPOJOBuilder(withPrefix = "")
private class FrameworkLessonBuilder(@JsonProperty(CONTENT) content: List<String?>) : LessonBuilder(content) {
  override fun createLesson(): Lesson = FrameworkLesson()
}

private class LessonTypePropertyWriter : VirtualBeanPropertyWriter {

  @Suppress("unused")
  constructor()

  constructor(propDef: BeanPropertyDefinition, contextAnnotations: Annotations, declaredType: JavaType) : super(propDef,
                                                                                                                contextAnnotations,
                                                                                                                declaredType)

  override fun withConfig(config: MapperConfig<*>?,
                          declaringClass: AnnotatedClass,
                          propDef: BeanPropertyDefinition,
                          type: JavaType): VirtualBeanPropertyWriter {
    return LessonTypePropertyWriter(propDef, declaringClass.annotations, type)
  }

  override fun value(bean: Any, gen: JsonGenerator, prov: SerializerProvider): Any {
    return FRAMEWORK_TYPE
  }
}
