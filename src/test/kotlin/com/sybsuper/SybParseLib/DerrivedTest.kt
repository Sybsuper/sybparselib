package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals

class DerrivedTest {
    @Test
    fun `many parser parses 0 or more`() {
        val parser = anySymbol<Char>()
        val manyParser = many(parser)

        val result = manyParser("aaa".toList() to 0)
        assertEquals(listOf("".toList() to 0, "a".toList() to 1, "aa".toList() to 2, "aaa".toList() to 3), result)
    }

    @Test
    fun `many parser parses 1 or more`() {
        val parser = anySymbol<Char>()
        val someParser = some(parser)

        val result = someParser("aaa".toList() to 0)
        assertEquals(listOf("a".toList() to 1, "aa".toList() to 2, "aaa".toList() to 3), result)
    }

    @Test
    fun `greedy parser parses as much as possible`() {
        val parser = anySymbol<Char>()
        val greedyParser = greedy(parser)

        val result = greedyParser("aaa".toList() to 0)
        assertEquals(listOf("aaa".toList() to 3), result)
    }

    @Test
    fun `greedy parser parses empty string`() {
        val parser = anySymbol<Char>()
        val greedyParser = greedy(parser)

        val result = greedyParser("".toList() to 0)
        assertEquals(listOf("".toList() to 0), result)
    }

    @Test
    fun `greedy parser handles multiple results from parser correctly`() {
        val parser = anySymbol<Char>() or anySymbol()
        val greedyParser = greedy(parser)

        val result = greedyParser("aaa".toList() to 0)
        assertEquals((0..7).map { "aaa".toList() to 3 }, result)
    }

    @Test
    fun `greedy1 parser parses as much as possible`() {
        val parser = anySymbol<Char>()
        val greedyParser = greedy1(parser)

        val result = greedyParser("aaa".toList() to 0)
        assertEquals(listOf("aaa".toList() to 3), result)
    }

    @Test
    fun `greedy1 parser does not parse empty string`() {
        val parser = anySymbol<Char>()
        val greedyParser = greedy1(parser)

        val result = greedyParser("".toList() to 0)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `greedy1 parser handles multiple results from parser correctly`() {
        val parser = anySymbol<Char>() or anySymbol()
        val greedyParser = greedy1(parser)

        val result = greedyParser("aaa".toList() to 0)
        assertEquals((0..7).map { "aaa".toList() to 3 }, result)
    }

    @Test
    fun `parserListOf handles multiple parser results`() {
        val parser = anySymbol<Char>() or anySymbol()
        val result = parserListOf(parser, token(",,".toList()) biasedOr symbol(','))("a,,b,c".toList() to 0)
        assertEquals((0..7).map { "abc".toList() to 6 }, result)
    }
}