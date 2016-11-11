package com.eugenesusla.kotlin

import com.eugenesusla.kotlin.json
import com.fasterxml.jackson.databind.JsonNode
import org.junit.Assert
import org.junit.Test

class JsonTest {
    @Test
    fun example() {
        json {
            "k1" %= "v1"
            "k2" %= 2.0
            "k3" %= true
            "k4" %= null
            "k5" %= json [1, 2]
        }.assertEquals("""{
            "k1": "v1",
            "k2": 2.0,
            "k3": true,
            "k4": null,
            "k5": [1, 2]
        }""")
    }
}

private fun JsonNode.assertEquals(jsonString: String) {
    Assert.assertEquals(com.eugenesusla.kotlin.json.objectMapper.readTree(jsonString), this)
}