package com.eugenesusla.kotlin

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

abstract class JsonImpl<JSON, OBJECT: JSON>(val jsonClass: Class<JSON>) {
    protected val converters = LinkedList<(Any) -> Any?>()
    /**
     * @return lambda returning null if can't handle given input
     */
    @SuppressWarnings("deprecation")
    inline fun <reified T> autoConvert(crossinline handler : (T) -> Any?) = addAutoConverter {
        (it as? T)?.let(handler)
    }
    @Deprecated("use autoConvert")
    fun addAutoConverter(handler : (Any) -> Any?) = converters.add(handler)

    internal fun handle(v: Any): JSON {
        try {
            converters.forEach { handler ->
                handler(v)?.let { return invoke(it) }
            }
            return fromProperties(v)
        } catch (e: Exception) {
            throw RuntimeException("Failed to handle ${v.toStringWithClass()}", e)
        }
    }

    fun fromFields(o: Any): OBJECT {
        val v = o.javaClass.allFields.map {
            it.isAccessible = true
            it.name to it.get(o)
        }.toTypedArray()
        return invoke(*v)
    }

    fun fromProperties(o: Any, ignoreClass: Boolean = true): OBJECT {
        val clazz = o.javaClass
        val props = if (ignoreClass) clazz.properties else clazz.propertiesIncludingClass
        val kv = props.map {
            it.getter.isAccessible = true
            val key = it.name
            var value = try {
                it.getter(o)
            } catch (e: ReflectiveOperationException) {
                throw ReflectiveOperationException(
                        "Failed to invoke ${it.getter}(${o.toStringWithClass()})", e)
            }
            if (value === o) {
//                throw IllegalStateException("Self reference by $key in $o")
                value = "\$self"
            }
            key to value
        }.toTypedArray()
        return invoke(*kv)
    }

    operator fun invoke(v: Any?): JSON {
//        println("json(${v.toStringWithClass()})")
        if (jsonClass.isInstance(v)) {
            return v as JSON
        }
        return when (v) {
            null -> ofNull()

            is String -> invoke(v)
            is Int -> invoke(v)
            is Long -> invoke(v)
            is Boolean -> invoke(v)
            is Double -> invoke(v)
            is Float -> invoke(v)
            is BigDecimal -> invoke(v)
            is BigInteger -> invoke(v)

            is Array<*> -> get(*v)
            is Iterable<*> -> get(*v.toList().toTypedArray())
            is Sequence<*> -> get(*v.toList().toTypedArray())
            is Map<*, *> -> invoke(
                (v as? Map<String, Any?>)
                    ?: v.mapKeys { it.toString() })

            else -> handle(v)
        }
    }


    abstract protected fun ofNull(): JSON
    abstract operator fun invoke(v: String): JSON
    abstract operator fun invoke(v: Boolean): JSON
    abstract operator fun invoke(v: Int): JSON
    abstract operator fun invoke(v: Long): JSON
    abstract operator fun invoke(v: Double): JSON
    abstract operator fun invoke(v: Float): JSON
    abstract operator fun invoke(v: BigDecimal): JSON
    abstract operator fun invoke(v: BigInteger): JSON

    abstract operator fun invoke(vararg children : Pair<String, Any?>): OBJECT
    abstract operator fun invoke(children : Map<String, Any?>): OBJECT
    abstract operator fun get(vararg children : Any?): JSON

    interface Populator {
        operator fun String.modAssign(v: Any?): Unit
    }
}
