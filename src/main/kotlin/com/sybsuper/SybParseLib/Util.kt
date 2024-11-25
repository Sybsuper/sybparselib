package com.sybsuper.SybParseLib

import java.util.*


fun <S> List<S>.startsWith(otherList: List<S>): Boolean {
    if (otherList.size > this.size)
        return false

    return otherList.indices.all { this[it] == otherList[it] }
}

