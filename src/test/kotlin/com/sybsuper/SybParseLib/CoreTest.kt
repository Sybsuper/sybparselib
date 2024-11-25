package com.sybsuper.SybParseLib

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

    @Test
    fun `and parser works correctly`() {
        val parserA = symbol('a')
        val parserB = symbol('b') or symbol('a')
        val parserC = symbol('c') or symbol('c')
        val parserD = symbol('d')

        val combinedParser = { a: Char, b: Char, c: Char, d: Char ->
            listOf(
                a,
                b,
                c,
                d
            )
        }.curried() applyWith parserA and parserB and parserC and parserD

        val result = combinedParser("abcd".toList() to 0)
        assertEquals(listOf("abcd".toList() to 4, "abcd".toList() to 4), result)
    }
}