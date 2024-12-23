package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTests {
    open class JsonObject {
        companion object : Parser<Char, JsonObject> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonObject> {
                val parser: Parser<Char, JsonObject> = JsonString or JsonInt or JsonDouble or JsonList or JsonMap
                return parser(input)
            }
        }

        override fun equals(other: Any?): Boolean {
            return false
        }
    }

    class JsonString(val value: String = "") : JsonObject() {
        companion object : Parser<Char, JsonString> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonString> {
                return escapedString(input).map { JsonString(it.first.joinToString(separator = "")) to it.second }
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonString && other.value == value
        }
    }

    class JsonList(
        val list: List<JsonObject> = emptyList()
    ) : JsonObject() {
        companion object : Parser<Char, JsonList> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonList> {
                val res =
                    { it: List<JsonObject> -> JsonList(it) } applyWith bracketed(
                        whitespaced(
                            parserListOf(
                                JsonObject,
                                whitespaced(symbol(','))
                            )
                        )
                    )
                return res(input)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonList && other.list == list
        }
    }

    class JsonMap(
        val map: Map<String, JsonObject> = emptyMap()
    ) : JsonObject() {
        companion object : Parser<Char, JsonMap> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonMap> {
                val keyValuePairParser =
                    ({ x: JsonString, y: JsonObject -> x.value to y }.curried() applyWith
                            (whitespaces andR JsonString)
                            andL whitespaced(symbol(':'))
                            and JsonObject)
                val res =
                    { it: List<Pair<String, JsonObject>> -> JsonMap(it.toMap()) } applyWith whitespaced(
                        braced(
                            parserListOf(
                                keyValuePairParser,
                                whitespaced(symbol(','))
                            )
                        )
                    )
                return res(input)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonMap && other.map == map
        }
    }

    class JsonInt(
        val value: Int = 0
    ) : JsonObject() {
        companion object : Parser<Char, JsonInt> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonInt> {
                val res = integer(input)
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
        companion object : Parser<Char, JsonDouble> {
            override fun invoke(input: ParserInput<Char>): ParserOutput<JsonDouble> {
                val res = { x: Int, y: Int ->
                    JsonDouble(
                        "$x.$y".toDouble()
                    )
                }.curried() applyWith integer andL symbol('.') and natural
                return res(input)
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is JsonDouble && other.value == value
        }
    }

    @Test
    fun `token parser should match a sequence of tokens`() {
        val parser = JsonObject

        val result = parser(
            ("{\n" +
                    "\t\"abc\": [1, 12.34   , -2.3 , -30,  {  \"a\":1}],\"b\":{\"c\":\"\\n\\td\"}}").toList() to 0
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
                            "c" to JsonString("\n\td")
                        )
                    )
                )
            ),
            result.firstOrNull()?.first
        )
    }
}