package com.sybsuper.SybParseLib.oop

typealias Parser<S, R> = (Pair<List<S>, Int>) -> List<Pair<R, Int>>

fun <S> anySymbol(): Parser<S, S> = { (ss, i) ->
    val x = ss.getOrNull(i)
    if (x == null) emptyList() else listOf(x to i + 1)
}

fun <S> satisfy(f: (S) -> Boolean): Parser<S, S> = { (ss, i) ->
    val x = ss.getOrNull(i)
    if (x == null || !f(x)) emptyList()
    else listOf(x to i + 1)
}

fun <S> symbol(s: S): Parser<S, S> = satisfy { it == s }

fun <S, A> succeed(r: A): Parser<S, A> = { (_, i) -> listOf(r to i) }

fun <S : Any, A : Any> empty(): Parser<S, A> = { _ -> emptyList() }

fun <A> look(): Parser<A, List<A>> = { (ss, i) -> listOf(ss to i) }

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

/** <*> */
infix fun <S, A, B> Parser<S, (B) -> A>.and(other: Parser<S, B>): Parser<S, A> = { (xs, i) ->
    val result = ArrayList<Pair<A, Int>>()
    this(xs to i).forEach { (f, j) ->
        other(xs to j).forEach { (x, k) ->
            result.add(f(x) to k)
        }
    }
    result
}

/** <|> */
infix fun <S, A> Parser<S, A>.or(other: Parser<S, A>): Parser<S, A> = { xs -> this(xs) + other(xs) }

/** <<|> */
infix fun <S, A> Parser<S, A>.biasedOr(other: Parser<S, A>): Parser<S, A> =
    { xs -> this(xs).ifEmpty { other(xs) } }

/** <$> */
infix fun <S, A, B> ((A) -> B).applyWith(parser: Parser<S, A>): Parser<S, B> =
    { xs -> parser(xs).map { (y, ys) -> this(y) to ys } }
