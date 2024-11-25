package com.sybsuper.SybParseLib.oop

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreTest {
    @Test
    fun `combined parsers work together as expected`() {
        val parserA = token("a".toList())
        val parserB = token("b".toList())
        val parserC = token("c".toList())
        val parserD = token("d".toList())

        val combinedParser = parserA andR parserB andL parserC or parserD

        val result = combinedParser("abcd".toList() to 0)
        assertEquals(listOf("b".toList() to 3), result)
    }
}