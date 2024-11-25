package com.sybsuper.SybParseLib

/**
 * A parser that satisfies a condition on characters.
 */
val digitParser = satisfy<Char> { it.isDigit() }

/**
 * A parser that converts a parsed digit character to its integer value.
 */
val newDigit = { res: Char -> res.digitToInt() } applyWith digitParser

/**
 * A parser that parses a sequence of digits and converts them to an integer.
 */
val natural = { res: List<Int> -> res.fold(0) { acc, x -> acc * 10 + x } } applyWith greedy(newDigit)

/**
 * A parser that parses an optional negative sign followed by a natural number, returning the integer value.
 */
val integer =
    { res: Char? -> { num: Int -> (if (res == '-') -1 else 1) * num } } applyWith optional(symbol('-')) and natural

/**
 * A parser that parses an identifier starting with a letter followed by letters or digits. ([a-zA-Z][a-zA-Z0-9]*)
 */
val identifier =
    { letter: Char -> { letters: List<Char> -> listOf(letter) + letters } } applyWith satisfy { it.isLetter() } and greedy(
        satisfy { it.isLetterOrDigit() })

/**
 * A parser that parses an expression enclosed in parentheses.
 * @param parser The parser for the expression inside the parentheses.
 * @return A parser that parses the expression inside parentheses.
 */
fun <A : Any> parenthesised(parser: Parser<Char, A>) = symbol('(') andR parser andL symbol(')')

/**
 * A parser that parses an expression enclosed in brackets.
 * @param parser The parser for the expression inside the brackets.
 * @return A parser that parses the expression inside brackets.
 */
fun <A : Any> bracketed(parser: Parser<Char, A>) = symbol('[') andR parser andL symbol(']')

/**
 * A parser that parses an expression enclosed in braces.
 * @param parser The parser for the expression inside the braces.
 * @return A parser that parses the expression inside braces.
 */
fun <A : Any> braced(parser: Parser<Char, A>) = symbol('{') andR parser andL symbol('}')

/**
 * A parser that parses a list of expressions separated by commas.
 * @param parser The parser for the individual expressions.
 * @return A parser that parses a comma-separated list of expressions.
 */
fun <A : Any> commaList(parser: Parser<Char, A>) = parserListOf(parser, symbol(','))

/**
 * A parser that parses a list of expressions separated by semicolons.
 * @param parser The parser for the individual expressions.
 * @return A parser that parses a semicolon-separated list of expressions.
 */
fun <A : Any> semiList(parser: Parser<Char, A>) = parserListOf(parser, symbol(';'))
