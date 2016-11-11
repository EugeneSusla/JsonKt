package com.eugenesusla.kotlin

import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Modifier.PUBLIC
import kotlin.properties.Delegates
import kotlin.reflect.KCallable

fun <T> Class<*>.all(declaredMembers: Class<*>.() -> Array<T>): List<T> {
    val mine = declaredMembers().asList()
    val others = superclass?.all(declaredMembers) ?: emptyList<T>()
    return others + mine
}

data class Property private constructor(
        val name: String,
        val getter: Method,
        val setter: Method?) {
    val owner = getter.declaringClass
    companion object {
        fun from(getter: Method): Property? {
            if (getter.parameterCount > 0) {
                return null
            }
            var propertyNameUpperCamel: String? = null
            val name = getter.name
            if (name.matches("^get[A-Z].*$".toRegex())) {
                propertyNameUpperCamel = name.substring(3)
            } else if (name.matches("^is[A-Z].*$".toRegex())) {
                propertyNameUpperCamel = name.substring(2)
            }
            return propertyNameUpperCamel?.let { name ->
                val propertyName = name.decapitalize()
                val setterName = "set" + name
                Property(
                        propertyName,
                        getter,
                        tryOrNull {
                            getter.declaringClass.getDeclaredMethod(
                                    setterName,
                                    getter.returnType)
                        })
            }
        }
    }
}

val Class<*>.allFields: List<Field>
    get() = all { declaredFields }
val Class<*>.allMethods: List<Method>
    get() = all { declaredMethods }
val Class<*>.propertiesIncludingClass: List<Property>
    get() = methods.mapNotNull { Property.from(it) }
val Class<*>.allPropertiesIncludingClass: List<Property>
    get() = allMethods.mapNotNull { Property.from(it) }
val Class<*>.allProperties: List<Property>
    get() = allPropertiesIncludingClass.filterNot { it.name == "class" }
val Class<*>.properties: List<Property>
    get() = allProperties.filter { it.getter.modifiers.contains(PUBLIC) }
