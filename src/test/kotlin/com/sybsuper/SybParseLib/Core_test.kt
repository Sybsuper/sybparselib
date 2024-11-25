package com.sybsuper.SybParseLib

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
class CoreTest {
    @Test
    fun `anySymbol should return empty list for empty input`() {
        val parser = anySymbol<Char>()
        val result = parser(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `anySymbol should return a list with a single pair when given a sequence with one element`() {
        val parser = anySymbol<Char>()
        val result = parser(listOf('a'))
        assertEquals(listOf('a' to emptyList()), result)
    }

    @Test
    fun `anySymbol should return the remaining sequence without the first element in the result pair`() {
        val parser = anySymbol<Char>()
        val input = listOf('a', 'b', 'c', 'd')
        val result = parser(input)
        assertEquals(listOf('a' to listOf('b', 'c', 'd')), result)
    }

    @Test
    fun `satisfy should return a list with one element when given a predicate that always returns true`() {
        val parser = satisfy<Char> { true }
        val input = listOf('a', 'b', 'c')
        val result = parser(input)
        assertEquals(listOf('a' to listOf('b', 'c')), result)
    }

    @Test
    fun `satisfy should return an empty list when given a predicate that always returns false`() {
        val parser = satisfy<Char> { false }
        val input = listOf('a', 'b', 'c')
        val result = parser(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `satisfy should return an empty list when given an empty list`() {
        val parser = satisfy<Char> { true }
        val input = emptyList<Char>()
        val result = parser(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `and operator should combine two parsers and return correct result for valid input`() {
        val parser1: Parser<Char, (Int) -> String> =
            { _ -> listOf({ i: Int -> i.toString() } to listOf('a', 'b', 'c')) }
        val parser2: Parser<Char, Int> = { _ -> listOf(42 to listOf('b', 'c')) }
        val combinedParser = parser1 andF parser2
        val result = combinedParser(listOf('x', 'y', 'z'))
        assertEquals(listOf("42" to listOf('b', 'c')), result)
    }

    @Test
    fun `and operator should return empty result when first parser fails`() {
        val parser1: Parser<Char, (Int) -> String> = { _ -> emptyList() }
        val parser2: Parser<Char, Int> = { _ -> listOf(42 to emptyList()) }
        val combinedParser = parser1 andF parser2
        val result = combinedParser(listOf('a', 'b', 'c'))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `and operator should return empty result when second parser fails`() {
        val parser1: Parser<Char, (Int) -> String> =
            { _ -> listOf({ i: Int -> i.toString() } to listOf('a', 'b', 'c')) }
        val parser2: Parser<Char, Int> = { _ -> emptyList() }
        val combinedParser = parser1 andF parser2
        val result = combinedParser(listOf('x', 'y', 'z'))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `and operator should handle nested operations correctly`() {
        val parser1: Parser<Char, (Int) -> (String) -> String> =
            { _ -> listOf({ i: Int -> { s: String -> "$i-$s" } } to listOf('a', 'b', 'c')) }
        val parser2: Parser<Char, Int> = { _ -> listOf(42 to listOf('b', 'c')) }
        val parser3: Parser<Char, String> = { _ -> listOf("test" to listOf('c')) }
        val combinedParser = (parser1 andF parser2) andF parser3
        val result = combinedParser(listOf('x', 'y', 'z'))
        assertEquals(listOf("42-test" to listOf('c')), result)
    }


    @Test
    fun `empty returns the empty list`() {
        val parser = empty<Char, Int>()
        val result = parser(listOf('a', 'b', 'c'))
        assertTrue(result.isEmpty())
    }



    @Test
    fun `look shows remaining tokens`() {
        val parser = look<Char>()
        val result = parser(listOf('a', 'b', 'c'))
        assertEquals(listOf(listOf('a', 'b', 'c') to listOf('a', 'b', 'c')), result)
    }

    @Test
    fun `pure returns given value and input`() {
        val parser = pure<Char, Int>(42)
        val result = parser(listOf('a', 'b', 'c'))
        assertEquals(listOf(42 to listOf('a', 'b', 'c')), result)
    }

    @Test
    fun `bind combines parsers correctly`() {
        val parser = bind(symbol('a')) { succeed<Char, Char>(it) }
        val result = parser(listOf('a', 'b', 'c'))
        assertEquals(listOf('a' to listOf('b', 'c')), result)
    }

    @Test
    fun `and combines parsers correctly`() {
        val parser = succeed<Char, (Char) -> Char> { it } andF symbol('a')
        val result = parser(listOf('a', 'b', 'c'))
        assertEquals(listOf('a' to listOf('b', 'c')), result)
    }

    @Test
    fun `or combines parsers correctly`() {
        val parser = symbol('a') or symbol('b')
        val result = parser(listOf('b', 'c', 'd'))
        assertEquals(listOf('b' to listOf('c', 'd')), result)
    }

    @Test
    fun `biasedOr takes first when successful`() {
        val parser = symbol('a') biasedOr symbol('b')
        val result = parser(listOf('a', 'b', 'c', 'd'))
        assertEquals(listOf('a' to listOf('b', 'c', 'd')), result)
    }

    @Test
    fun `biasedOr takes second when first fails`() {
        val parser = symbol('a') biasedOr symbol('b')
        val result = parser(listOf('b', 'c', 'd'))
        assertEquals(listOf('b' to listOf('c', 'd')), result)
    }

    @Test
    fun `applyWith applies function to parser result`() {
        val parser = {it:Char-> it.uppercaseChar() } applyWith anySymbol()
        val result = parser(listOf('a', 'b', 'c'))
        assertEquals(listOf('A' to listOf('b', 'c')), result)
    }

}
