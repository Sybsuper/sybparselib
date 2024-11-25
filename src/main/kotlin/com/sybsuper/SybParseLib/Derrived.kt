package com.sybsuper.SybParseLib

/**
 * Creates a parser that matches a specific sequence of tokens.
 *
 * @param input The sequence of tokens to match.
 * @return A parser that matches the given sequence of tokens.
 */
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

/**
 * Creates a parser that matches a sequence of three parsers, returning the result of the middle parser.
 *
 * @param p The first parser.
 * @param r The second parser.
 * @param q The third parser.
 * @return A parser that matches the sequence of `p`, `r`, and `q`, returning the result of `r`.
 */
fun <S, A, B : Any, C : Any> pack(p: Parser<S, A>, r: Parser<S, B>, q: Parser<S, C>): Parser<S, B> = p andR r andL q

/**
 * Creates a parser that matches a sequence of parsers.
 *
 * @param parsers The list of parsers to match in sequence.
 * @return A parser that matches the sequence of parsers.
 */
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

/**
 * Creates a parser that matches any one of the given parsers.
 *
 * @param parsers The list of parsers to choose from.
 * @return A parser that matches any one of the given parsers.
 */
fun <S : Any, A : Any> choice(parsers: List<Parser<S, A>>): Parser<S, A> = parsers.foldRight(empty()) { a, b -> a or b }

/**
 * Creates a parser that matches the given parser or returns a default value.
 *
 * @param parser The parser to match.
 * @param default The default value to return if the parser does not match.
 * @return A parser that matches the given parser or returns the default value.
 */
fun <S : Any, A : Any> option(parser: Parser<S, A>, default: A): Parser<S, A> = parser or succeed(default)

/**
 * Creates a parser that optionally matches the given parser.
 *
 * @param parser The parser to optionally match.
 * @return A parser that optionally matches the given parser.
 */
fun <S, A> optional(parser: Parser<S, A>): Parser<S, A?> = { xs ->
    val result = parser(xs)
    if (result.isEmpty()) listOf(null to xs.second) else result.map { it.first to it.second }
}

/**
 * Creates a parser that matches zero or more occurrences of the given parser.
 *
 * @param parser The parser to match zero or more times.
 * @return A parser that matches zero or more occurrences of the given parser.
 */
fun <S, A> many(parser: Parser<S, A>): Parser<S, List<A>> = parseMultiple(parser, true)

/**
 * Creates a parser that matches one or more occurrences of the given parser.
 *
 * @param parser The parser to match one or more times.
 * @return A parser that matches one or more occurrences of the given parser.
 */
fun <S, A> some(parser: Parser<S, A>): Parser<S, List<A>> = parseMultiple(parser, false)

/**
 * Helper function to create a parser that matches multiple occurrences of the given parser.
 *
 * @param parser The parser to match multiple times.
 * @param allowEmpty Whether to allow zero occurrences.
 * @return A parser that matches multiple occurrences of the given parser.
 */
private fun <S, A> parseMultiple(parser: Parser<S, A>, allowEmpty: Boolean): Parser<S, List<A>> = { input ->
    val results = mutableListOf<Pair<List<A>, Int>>()
    if (allowEmpty) results.add(emptyList<A>() to input.second)
    var rest = input
    while (true) {
        val parsed = parser(rest)
        if (parsed.isEmpty()) break
        for ((value, nextIndex) in parsed) {
            results.add((results.lastOrNull()?.first ?: emptyList()) + value to nextIndex)
            rest = input.first to nextIndex
        }
    }
    results
}

//fun <S,A : Any,B> parserListOfEquivalent(parser: Parser<S, A>, separator: Parser<S, B>): Parser<S, List<A>> = { x:A -> { y: List<A> -> listOf(x) + y}} applyWith parser and greedy(separator andR parser)

/**
 * Creates a parser that matches a list of `parser`s separated by `separator`s.
 *
 * @param parser The parser to match.
 * @param separator The parser for the separator.
 * @return A parser that matches a list of `parser`s separated by `separator`s.
 */
fun <S, A : Any, B : Any> parserListOf(parser: Parser<S, A>, separator: Parser<S, B>): Parser<S, List<A>> {
    return fun(input: Pair<List<S>, Int>): MutableList<Pair<List<A>, Int>> {
        val results = mutableListOf<Pair<List<A>, Int>>()
        val initialParsed = parser(input)
        if (initialParsed.isEmpty()) return mutableListOf()

        for ((firstValue, firstIndex) in initialParsed) {
            val stack = mutableListOf<Pair<List<A>, Int>>()
            stack.add(listOf(firstValue) to firstIndex)

            while (stack.isNotEmpty()) {
                val (currentList, currentIndex) = stack.removeAt(stack.size - 1)
                val rest = input.first to currentIndex
                val separatorParsed = separator(rest)

                if (separatorParsed.isEmpty()) {
                    results.add(currentList to currentIndex)
                } else {
                    for ((_, sepNextIndex) in separatorParsed) {
                        val nextParsed = parser(input.first to sepNextIndex)
                        if (nextParsed.isEmpty()) {
                            results.add(currentList to currentIndex)
                        } else {
                            for ((nextValue, nextIndex) in nextParsed) {
                                stack.add((currentList + nextValue) to nextIndex)
                            }
                        }
                    }
                }
            }
        }

        return results
    }
}

/**
 * Creates a parser that matches the longest sequence of matches of the given parser.
 *
 * @param parser The parser to match greedily.
 * @return A parser that matches the longest sequence of matches of the given parser.
 */
fun <S, A> greedy(parser: Parser<S, A>): Parser<S, List<A>> = { input ->
    val results = mutableListOf<Pair<List<A>, Int>>()
    val stack = mutableListOf<Pair<List<A>, Int>>()
    stack.add(emptyList<A>() to input.second)

    while (stack.isNotEmpty()) {
        val (currentList, currentIndex) = stack.removeAt(stack.size - 1)
        val rest = input.first to currentIndex
        val parsed = parser(rest)

        if (parsed.isEmpty()) {
            results.add(currentList to currentIndex)
        } else {
            for ((value, nextIndex) in parsed) {
                stack.add((currentList + value) to nextIndex)
            }
        }
    }

    results
}

/**
 * Creates a parser that matches the longest sequence of matches of the given parser, but requires at least one match.
 *
 * @param parser The parser to match greedily.
 * @return A parser that matches the longest sequence of matches of the given parser, but requires at least one match.
 */
fun <S, A> greedy1(parser: Parser<S, A>): Parser<S, List<A>> = {
    val results = mutableListOf<Pair<List<A>, Int>>()
    val stack = mutableListOf<Pair<List<A>, Int>>()
    if (parser(it).isNotEmpty()) stack.add(emptyList<A>() to it.second)

    while (stack.isNotEmpty()) {
        val (currentList, currentIndex) = stack.removeAt(stack.size - 1)
        val rest = it.first to currentIndex
        val parsed = parser(rest)

        if (parsed.isEmpty()) {
            if (currentList.isNotEmpty()) {
                results.add(currentList to currentIndex)
            }
        } else {
            for ((value, nextIndex) in parsed) {
                stack.add((currentList + value) to nextIndex)
            }
        }
    }

    results
}

/**
 * Creates a parser that applies a constant value and then matches the given parser.
 *
 * A.K.A. `<$`
 *
 * @param p The parser to match.
 * @return A parser that applies a constant value and then matches the given parser.
 */
infix fun <S, A, B : Any> B.applyL(p: Parser<S, A>): Parser<S, B> {
    val const = ({ b: B, _: A -> b }).curried()
    return const(this) applyWith p
}

/**
 * Creates a parser that matches the first parser and then the second parser, returning the result of the first parser.
 *
 * A.K.A. `<*`
 *
 * @param q The second parser.
 * @return A parser that matches the first parser and then the second parser, returning the result of the first parser.
 */
infix fun <S, A, B : Any> Parser<S, A>.andL(q: Parser<S, B>): Parser<S, A> {
    val const = ({ a: A, _: B -> a }).curried()
    return const applyWith this and q
}

/**
 * Creates a parser that matches the first parser and then the second parser, returning the result of the second parser.
 *
 * A.K.A. `*>`
 *
 * @param q The second parser.
 * @return A parser that matches the first parser and then the second parser, returning the result of the second parser.
 */
infix fun <S, A, B : Any> Parser<S, A>.andR(q: Parser<S, B>): Parser<S, B> {
    val constFlipped = ({ _: A, b: B -> b }).curried()
    return constFlipped applyWith this and q
}