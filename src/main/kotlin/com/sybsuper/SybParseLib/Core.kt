package com.sybsuper.SybParseLib

typealias Parser<S, R> = (List<S>) -> List<Pair<R, List<S>>>

/**
 * Returns a parser that matches any single symbol from the input list.
 *
 * @param S the type of the symbols in the input list
 * @return a parser that returns the first symbol and the rest of the list, or an empty list if the input is empty
 */
fun <S> anySymbol(): Parser<S, S> = { ss ->
    val x = ss.firstOrNull()
    if (x == null) emptyList() else listOf(x to ss.drop(1))
}

/**
 * Returns a parser that matches a single symbol satisfying the given predicate.
 *
 * @param S the type of the symbols in the input list
 * @param f the predicate that the symbol must satisfy
 * @return a parser that returns the first symbol satisfying the predicate and the rest of the list, or an empty list if no such symbol is found
 */
fun <S> satisfy(f: (S) -> Boolean): Parser<S, S> = { ss ->
    val x = ss.firstOrNull()
    if (x == null || !f(x)) emptyList()
    else listOf(x to ss.drop(1))
}

/**
 * Returns a parser that matches a specific symbol.
 *
 * @param S the type of the symbols in the input list
 * @param s the symbol to match
 * @return a parser that returns the matched symbol and the rest of the list, or an empty list if the symbol is not found
 */
fun <S> symbol(s: S): Parser<S, S> = satisfy { it == s }

fun <S, A> succeed(r: A): Parser<S, A> = { xs -> listOf(r to xs) }

fun <S : Any, A : Any> empty(): Parser<S, A> = { _ -> emptyList() }

fun <A> look(): Parser<A, List<A>> = { ss -> listOf(ss to ss) }

fun <S : Any, A : Any> pure(a: A): Parser<S, A> = succeed(a)

/** >>= */
fun <S, A, B> bind(p: Parser<S, A>, f: (A) -> Parser<S, B>): Parser<S, B> =
    { ss -> p(ss).flatMap { (a, ss2) -> f(a)(ss2) } }

/** <*> */
infix fun <S, A, B> Parser<S, (B) -> A>.andF(other: Parser<S, B>): Parser<S, A> = { xs ->
    val result = ArrayList<Pair<A, List<S>>>()
    this(xs).forEach { (f, ys) ->
        other(ys).forEach { (x, zs) ->
            result.add(f(x) to zs)
        }
    }
    result
}

infix fun <S, A> Parser<S, A>.and(other: Parser<S, A>): Parser<S,A> = { xs ->
    val result = ArrayList<Pair<A, List<S>>>()
    this(xs).forEach { (a, ys) ->
        other(ys).forEach { (b, zs) ->
            result.add(a to zs)
        }
    }
    result
}

/** <|> */
infix fun <S, A> Parser<S, A>.or(other: Parser<S, A>): Parser<S, A> =
    { xs -> this(xs) + other(xs) }

/** <<|> */
infix fun <S, A> Parser<S, A>.biasedOr(other: Parser<S, A>): Parser<S, A> =
    { xs -> this(xs).ifEmpty { other(xs) } }

/** <$> */
infix fun <S, A, B> ((A) -> B).applyWith(parser: Parser<S, A>): Parser<S, B> =
    { xs -> parser(xs).map { (y, ys) -> this(y) to ys } }