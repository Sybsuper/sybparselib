package com.sybsuper.SybParseLib

typealias Parser<S, R> = (Pair<List<S>, Int>) -> List<Pair<R, Int>>

/**
 * A parser that matches any symbol.
 * @return A parser that returns the matched symbol and the next index.
 */
fun <S> anySymbol(): Parser<S, S> = { (ss, i) ->
    val x = ss.getOrNull(i)
    if (x == null) emptyList() else listOf(x to i + 1)
}

/**
 * A parser that matches a symbol satisfying a given predicate.
 * @param f The predicate to satisfy.
 * @return A parser that returns the matched symbol and the next index if the predicate is satisfied.
 */
fun <S> satisfy(f: (S) -> Boolean): Parser<S, S> = { (ss, i) ->
    val x = ss.getOrNull(i)
    if (x == null || !f(x)) emptyList()
    else listOf(x to i + 1)
}

/**
 * A parser that matches a specific symbol.
 * @param s The symbol to match.
 * @return A parser that returns the matched symbol and the next index if the symbol is matched.
 */
fun <S> symbol(s: S): Parser<S, S> = satisfy { it == s }

/**
 * A parser that always succeeds with a given result.
 * @param r The result to return.
 * @return A parser that returns the given result and the current index.
 */
fun <S, A> succeed(r: A): Parser<S, A> = { (_, i) -> listOf(r to i) }

/**
 * A parser that always fails.
 * @return A parser that returns an empty list.
 */
fun <S : Any, A : Any> empty(): Parser<S, A> = { _ -> emptyList() }

/**
 * A parser that returns the current input without consuming any symbols.
 * @return A parser that returns the current input and the current index.
 */
fun <A> look(): Parser<A, List<A>> = { (ss, i) -> listOf(ss to i) }

/**
 * Parses the input using the given parser.
 * @param parser The parser to use.
 * @param input The input to parse.
 * @return A list of parsed results.
 */
fun <S, R> parse(parser: Parser<S, R>, input: List<S>): List<R> {
    val result = ArrayList<R>()
    var remainingInput = input
    var i = 0
    while (i < input.size) {
        val parsed = parser(remainingInput to i)
        if (parsed.isEmpty()) break
        val (value, rest) = parsed.first()
        result.add(value)
        remainingInput = input
        i = rest
    }
    return result
}

/**
 * Combines two parsers sequentially, applying the first parser and then the second parser.
 *
 * A.K.A <*>
 *
 * @param other The second parser to apply.
 * @return A parser that applies the first parser and then the second parser.
 */
infix fun <S, A, B> Parser<S, (B) -> A>.and(other: Parser<S, B>): Parser<S, A> = { (xs, i) ->
    val result = mutableListOf<Pair<A, Int>>()
    this(xs to i).forEach { (f, j) ->
        other(xs to j).forEach { (x, k) ->
            result.add(f(x) to k)
        }
    }
    result
}

/**
 * Combines two parsers, trying the first parser and if it fails, trying the second parser.
 *
 * A.K.A <|>
 *
 * @param other The second parser to try if the first parser fails.
 * @return A parser that tries the first parser and if it fails, tries the second parser.
 */
infix fun <S, A> Parser<S, A>.or(other: Parser<S, A>): Parser<S, A> = { xs -> this(xs) + other(xs) }

/**
 * Combines two parsers, trying the first parser and if it fails, trying the second parser.
 * The second parser is only tried if the first parser returns an empty list.
 *
 * A.K.A <<|>
 *
 * @param other The second parser to try if the first parser fails.
 * @return A parser that tries the first parser and if it fails, tries the second parser.
 */
infix fun <S, A> Parser<S, A>.biasedOr(other: Parser<S, A>): Parser<S, A> = { xs -> this(xs).ifEmpty { other(xs) } }

/**
 * Applies a function to the result of a parser.
 *
 * A.K.A <$>
 *
 * @param parser The parser to apply the function to.
 * @return A parser that applies the function to the result of the parser.
 */
infix fun <S, A, B> ((A) -> B).applyWith(parser: Parser<S, A>): Parser<S, B> =
    { xs -> parser(xs).map { (y, ys) -> this(y) to ys } }
