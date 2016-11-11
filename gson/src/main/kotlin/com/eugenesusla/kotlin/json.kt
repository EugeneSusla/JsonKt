package com.eugenesusla.kotlin

import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger

class gson private constructor() {
    companion object : JsonImpl<JsonElement, JsonObject>(JsonElement::class.java) {
        override fun ofNull() = JsonNull.INSTANCE!!
        override fun invoke(v: String) = JsonPrimitive(v)
        override fun invoke(v: Boolean) = JsonPrimitive(v)
        override fun invoke(v: Int) = JsonPrimitive(v)
        override fun invoke(v: Long) = JsonPrimitive(v)
        override fun invoke(v: Double) = JsonPrimitive(v)
        override fun invoke(v: Float) = JsonPrimitive(v)
        override fun invoke(v: BigDecimal) = JsonPrimitive(v)
        override fun invoke(v: BigInteger) = JsonPrimitive(v)
        override fun invoke(vararg children: Pair<String, Any?>) = gson {
            for ((k, v) in children) {
                k %= v
            }
        }
        override fun invoke(children: Map<String, Any?>) = gson {
            for ((k, v) in children) {
                k %= v
            }
        }

        override fun get(vararg children : Any?): JsonArray {
            val node = JsonArray()
            for (child in children) {
                node.add(Companion(child))
            }
            return node
        }
    }
}

inline fun gson(populate: JsonImpl.Populator.() -> Unit): JsonObject {
    return ObjectBuilder(JsonObject())
            .apply(populate)
            .delegate
}

class ObjectBuilder(val delegate: JsonObject): JsonImpl.Populator {
    override operator fun String.modAssign(v: Any?) {
        delegate.add(this, gson(v))
    }
}