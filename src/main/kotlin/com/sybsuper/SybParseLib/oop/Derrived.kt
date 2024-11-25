package com.sybsuper.SybParseLib.oop

import com.sybsuper.SybParseLib.curried

fun <S> token(input: List<S>): Parser<S, List<S>> = { (xs, i) ->
    var match = true
    for (j in input.indices) {
        val x = xs.getOrNull(i + j)
        if (x == null || x != input[j]) {
            match = false
            break
        }
    }
    if (match) listOf(input to i + input.size) else emptyList()
}

fun <S, A, B: Any, C: Any> pack(p: Parser<S, A>, r: Parser<S, B>, q: Parser<S, C>): Parser<S, B> =
    p andR r andL q

fun <S, A> sequence(parsers: List<Parser<S, A>>): Parser<S, List<A>> {
    return fun(xs: Pair<List<S>, Int>): List<Pair<List<A>, Int>> {
        val initialResult = mutableListOf(mutableListOf<A>() to xs.second)

        for (parser in parsers) {
            val newResults = mutableListOf<Pair<MutableList<A>, Int>>()
            for ((result, i) in initialResult) {
                val parsed = parser(xs.first to i)
                for ((value, rest) in parsed) {
                    val newResult = result.toMutableList()
                    newResult.add(value)
                    newResults.add(newResult to rest)
                }
            }
            if (newResults.isEmpty()) return emptyList()
            initialResult.clear()
            initialResult.addAll(newResults)
        }

        return initialResult
    }
}


fun <S : Any, A : Any> choice(parsers: List<Parser<S, A>>): Parser<S, A> =
    parsers.foldRight(empty()) { a, b -> a or b }

fun <S : Any, A : Any> option(parser: Parser<S, A>, default: A): Parser<S, A> = parser or succeed(default)

fun <S, A> optional(parser: Parser<S, A>): Parser<S,A?> = {xs ->
    val result = parser(xs)
    if (result.isEmpty()) listOf(null to xs.second) else result.map { it.first to it.second }
}

/** <$ */
infix fun <S, A, B : Any> B.applyL(p: Parser<S, A>): Parser<S, B> {
    val const = ({ b: B, _: A -> b }).curried()
    return const(this) applyWith p
}

/** <* */
infix fun <S, A, B : Any> Parser<S, A>.andL(q: Parser<S, B>): Parser<S, A> {
    val const = ({ a: A, _: B -> a }).curried()
    return const applyWith this and q
}

/** *> */
infix fun <S, A, B : Any> Parser<S, A>.andR(q: Parser<S, B>): Parser<S, B> {
    val constFlipped = ({ _: A, b: B -> b }).curried()
    return constFlipped applyWith this and q
}