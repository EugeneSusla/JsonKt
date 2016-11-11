package com.eugenesusla.kotlin

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal
import java.math.BigInteger

class json private constructor() {
    companion object : JsonImpl<JsonNode, ObjectNode>(JsonNode::class.java) {
        val objectMapper by lazy { ObjectMapper() }
        internal val prettyWriter by lazy {
            objectMapper.writer(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.Lf2SpacesIndenter())
                })
        }

        override fun ofNull() = NullNode.instance
        override fun invoke(vararg children: Pair<String, Any?>) = json {
            for ((k, v) in children) {
                put(k) { it %= v }
            }
        }
        override fun invoke(children: Map<String, Any?>) = json {
            for ((k, v) in children) {
                put(k) { it %= v }
            }
        }
        private fun Populator.put(k: String, put: Populator.(String) -> Unit) {
            try {
                put(k)
            } catch (e: Exception) {
                throw RuntimeException("Failed to initialize key $k", e)
            }
        }
        override fun invoke(v: String) = TextNode(v)
        override fun invoke(v: Boolean) = BooleanNode.valueOf(v)!!
        override fun invoke(v: Int) = IntNode(v)
        override fun invoke(v: Long) = LongNode(v)
        override fun invoke(v: Double) = DoubleNode(v)
        override fun invoke(v: Float) = DoubleNode(v.toDouble())
        override fun invoke(v: BigDecimal) = DecimalNode(v)
        override fun invoke(v: BigInteger) = BigIntegerNode(v)

        override fun get(vararg children : Any?): ArrayNode {
            val node = ArrayNode(JsonNodeFactory.instance)
            for (child in children) {
                node.add(invoke(child))
            }
            return node
        }
    }
}

fun json(v: ByteArray) = BinaryNode(v)

fun JsonNode.toPrettyString(): String {
    return json.prettyWriter.writeValueAsString(this)
}

inline fun json(
        factory: JsonNodeFactory = JsonNodeFactory.instance,
        populate: JsonImpl.Populator.() -> Unit): ObjectNode {
    return ObjectBuilder(ObjectNode(factory))
            .apply(populate)
            .delegate
}
class ObjectBuilder(val delegate: ObjectNode): JsonImpl.Populator {
    override operator fun String.modAssign(v: Any?) {
        delegate.put(this, json(v))
    }
}

//inline fun json(
//        factory: JsonNodeFactory = JsonNodeFactory.instance,
//        populate: JsonImpl.Populator.() -> Unit): ObjectNode {
//    return ObjectNodeWithOperatorSupport(factory).apply(populate)
//}
//open class ObjectNodeWithOperatorSupport(factory: JsonNodeFactory) :
//        ObjectNode(factory), JsonImpl.Populator {
//
//    override operator fun String.modAssign(v: Any?) {
//        this@ObjectNodeWithOperatorSupport.put(this, json(v))
//    }
//}

//fun ObjectNode.asMap(): Map<String, JsonNode?> = ObjectNodeMap(this)
//
//class ObjectNodeMap(val node: ObjectNode): AbstractMap<String, JsonNode?>() {
//    override val entries: MutableSet<MutableMap.MutableEntry<String, JsonNode?>>
//        get() = node.fields().asSequence().toMutableSet()
//}
