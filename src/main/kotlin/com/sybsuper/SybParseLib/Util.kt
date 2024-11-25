package com.sybsuper.SybParseLib


fun <S> List<S>.startsWith(otherList: List<S>): Boolean {
    if (otherList.size > this.size) return false

    return otherList.indices.all { this[it] == otherList[it] }
}

