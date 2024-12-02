package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTests {
    open class JsonObject {
        companion object {
            private fun jsonParser(input: Pair<List<Char>, Int>): List<Pair<JsonObject, Int>> {
                val parser: Parser<Char, JsonObject> =
                    JsonString.Companion::parse or
                            JsonInt::parse or
                            JsonDouble::parse or
                            JsonList::parse or
                            JsonMap::parse
                return parser(input)
            }

            val jsonParser: Parser<Char, JsonObject> = ::jsonParser
        }

        override fun equals(other: Any?): Boolean {
            return false
        }
    }

    class JsonString(val value: String = "") : JsonObject() {
        companion object {
            fun parse(xs: Pair<List<Char>, Int>): List<Pair<JsonString, Int>> {
                val res = pack(symbol('"'), greedy(satisfy { it != '"' }), symbol('"'))(xs)
                return res.map { JsonString(it.first.joinToString(separator = "")) to it.second }
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonString && other.value == value
        }
    }

    class JsonList(
        val list: List<JsonObject> = emptyList()
    ) : JsonObject() {
        companion object {
            fun parse(xs: Pair<List<Char>, Int>): List<Pair<JsonList, Int>> {
                val res =
                    { it: List<JsonObject> -> JsonList(it) } applyWith bracketed(
                        whitespaced(parserListOf(
                            jsonParser,
                            whitespaced(symbol(','))
                        ))
                    )
                return res(xs)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonList && other.list == list
        }
    }

    class JsonMap(
        val map: Map<String, JsonObject> = emptyMap()
    ) : JsonObject() {
        companion object {
            fun parse(xs: Pair<List<Char>, Int>): List<Pair<JsonMap, Int>> {
                val res =
                    { it: List<Pair<String, JsonObject>> -> JsonMap(it.toMap()) } applyWith whitespaced(braced(
                        parserListOf(
                            { x: JsonString, y: JsonObject -> x.value to y }.curried() applyWith (whitespaces andR JsonString::parse)
                                    andL whitespaced(symbol(':')) and jsonParser,
                            whitespaced(symbol(','))
                        )
                    ))
                return res(xs)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonMap && other.map == map
        }
    }

    class JsonInt(
        val value: Int = 0
    ) : JsonObject() {
        companion object {
            fun parse(xs: Pair<List<Char>, Int>): List<Pair<JsonInt, Int>> {
                val res = integer(xs)
                return res.map { JsonInt(it.first) to it.second }
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonInt && other.value == value
        }
    }

    class JsonDouble(
        val value: Double = 0.0
    ) : JsonObject() {
        companion object {
            fun parse(xs: Pair<List<Char>, Int>): List<Pair<JsonDouble, Int>> {
                val res = { x: Int, y: Int ->
                    JsonDouble(
                        "$x.$y".toDouble()
                    )
                }.curried() applyWith integer andL symbol('.') and natural
                return res(xs)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonDouble && other.value == value
        }
    }

    @Test
    fun `token parser should match a sequence of tokens`() {
        val parser = JsonObject.jsonParser

        val result = parser(
            ("{\n" +
                    "\t\"abc\": [1, 12.34   , -2.3 , -30,  {  \"a\":1}],\"b\":{\"c\":\"d\"}}").toList() to 0
        )

        assertEquals(
            JsonMap(
                mapOf(
                    "abc" to JsonList(
                        listOf(
                            JsonInt(1),
                            JsonDouble(12.34),
                            JsonDouble(-2.3),
                            JsonInt(-30),
                            JsonMap(
                                mapOf(
                                    "a" to JsonInt(1)
                                )
                            )
                        )
                    ),
                    "b" to JsonMap(
                        mapOf(
                            "c" to JsonString("d")
                        )
                    )
                )
            ),
            result.firstOrNull()?.first
        )
    }
}