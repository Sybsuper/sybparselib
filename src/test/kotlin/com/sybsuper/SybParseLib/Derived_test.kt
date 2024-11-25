package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals

class Derived_test {
    @Test
    fun `sequence should combine parsers using and`() {
        val parser1 = symbol('a')
        val parser2 = symbol('b')
        val parser3 = symbol('c')
        val parser = sequence(listOf(parser1, parser2, parser3))
        val result = parser("abc".toList())
        assertEquals(listOf(listOf('a', 'b', 'c') to emptyList()), result)
    }

    @Test
    fun `sequence should not return anything when last parser fails`() {
        val parser1 = symbol('a')
        val parser2 = symbol('b')
        val parser3 = symbol('c')
        val parser = sequence(listOf(parser1, parser2, parser3))
        val result = parser("abd".toList())
        assertEquals(emptyList(), result)
    }
}