package blogify.reflect.analysis.extensions

import blogify.reflect.analysis.ClassAnalysisCache
import blogify.reflect.models.Mapped

import kotlin.reflect.KClass

val KClass<out Mapped>.descriptor get() = ClassAnalysisCache[this]
