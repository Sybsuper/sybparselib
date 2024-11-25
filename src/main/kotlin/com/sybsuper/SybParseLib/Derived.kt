package com.sybsuper.SybParseLib

import java.util.*

fun <S : Any> token(input: List<S>): Parser<S, List<S>> = { ss ->
    if (ss.startsWith(input)) listOf(input to ss.drop(input.size))
    else emptyList()
}

fun <S, A : Any, B : Any, C : Any> pack(p: Parser<S, A>, r: Parser<S, B>, q: Parser<S, C>): Parser<S, B> =
    p andR r andL q

fun <S, A> sequenceRecursive(parsers: List<Parser<S, A>>): Parser<S, LinkedList<A>> {
    val insert = { a: A -> { bs: LinkedList<A> ->
        bs.addFirst(a)
        bs
    }}
    return if (parsers.isEmpty()) succeed(LinkedList<A>())
    else insert applyWith parsers.first() andF sequenceRecursive(parsers.drop(1))
}

fun <S, A> sequence(parsers: List<Parser<S, A>>): Parser<S, LinkedList<A>> {
    return fun(input: List<S>): List<Pair<LinkedList<A>, List<S>>> {
        val result = LinkedList<A>()
        var remainingInput = input
        for (parser in parsers) {
            val parsed = parser(remainingInput)
            if (parsed.isEmpty()) return emptyList()
            val (value, rest) = parsed.first()
            result.add(value)
            remainingInput = rest
        }
        return listOf(result to remainingInput)
    }
}

fun <S : Any, A : Any> choice(parsers: List<Parser<S, A>>): Parser<S, A> =
    parsers.foldRight(empty()) { a, b -> a or b }

fun <S : Any, A : Any> option(parser: Parser<S, A>, default: A): Parser<S, A> = parser or succeed(default)

/** <$ */
infix fun <S, A, B : Any> B.applyL(p: Parser<S, A>): Parser<S, B> {
    val const = ({ b: B, _: A -> b }).curried()
    return const(this) applyWith p
}

/** <* */
infix fun <S, A, B : Any> Parser<S, A>.andL(q: Parser<S, B>): Parser<S, A> {
    val const = ({ a: A, _: B -> a }).curried()
    return const applyWith this andF q
}

/** *> */
infix fun <S, A, B : Any> Parser<S, A>.andR(q: Parser<S, B>): Parser<S, B> {
    val constFlipped = ({ _: A, b: B -> b }).curried()
    return constFlipped applyWith this andF q
}