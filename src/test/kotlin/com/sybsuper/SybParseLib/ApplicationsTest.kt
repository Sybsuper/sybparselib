package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationsTest {
    @Test
    fun `digitParser matches digit`() {
        val result = digitParser(listOf('5') to 0)
        assertEquals(listOf('5' to 1), result)
    }

    @Test
    fun `digitParser does not match non digit`() {
        val result = digitParser(listOf('a') to 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `newdigit parses digit to int`() {
        val result = newDigit(listOf('3') to 0)
        assertEquals(listOf(3 to 1), result)
    }

    @Test
    fun naturalParsesMultipleDigits() {
        val result = natural("123".toList() to 0)
        assertEquals(listOf(123 to 3), result)
    }

    @Test
    fun integerParsesNegativeNumber() {
        val result = integer("-42".toList() to 0)
        assertEquals(listOf(-42 to 3), result)
    }

    @Test
    fun `integer parses positive number`() {
        val result = integer("56".toList() to 0)
        assertEquals(listOf(56 to 2), result)
    }

    @Test
    fun `identifier parses valid identifier`() {
        val result = identifier("a1b2".toList() to 0)
        assertEquals(listOf("a1b2".toList() to 4), result)
    }

    @Test
    fun `identifier does not parse invalid identifier`() {
        val result = identifier("1a1b2".toList() to 0)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `parenthesised parses content within parentheses`() {
        val parser = symbol('a')
        val result = parenthesised(parser)("(a)".toList() to 0)
        assertEquals(listOf('a' to 3), result)
    }

    @Test
    fun `bracketed parses content within brackets`() {
        val parser = symbol('b')
        val result = bracketed(parser)("[b]".toList() to 0)
        assertEquals(listOf('b' to 3), result)
    }

    @Test
    fun `braced parses content within braces`() {
        val parser = symbol('c')
        val result = braced(parser)("{c}".toList() to 0)
        assertEquals(listOf('c' to 3), result)
    }

    @Test
    fun `commaList parses comma separated values`() {
        val parser = anySymbol<Char>()
        val result = parse(commaList(parser), "a,b,c".toList())
        assertEquals(listOf("abc".toList()), result)
    }

    @Test
    fun `semiList parses semicolon separated values`() {
        val parser = anySymbol<Char>()
        val result = parse(semiList(parser), "a;b;c".toList())
        assertEquals(listOf("abc".toList()), result)
    }
}