package dev.kuro9.domain.database

import org.jetbrains.exposed.sql.Between
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between

infix fun <T : Comparable<T>, S : T?> ExpressionWithColumnType<in S>.between(range: ClosedRange<T>): Between {
    return this.between(range.start, range.endInclusive)
}