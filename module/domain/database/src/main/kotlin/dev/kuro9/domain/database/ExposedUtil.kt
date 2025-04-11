package dev.kuro9.domain.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between

infix fun <T : Comparable<T>, S : T?> ExpressionWithColumnType<in S>.between(range: ClosedRange<T>): Between {
    return this.between(range.start, range.endInclusive)
}

fun Query.fetchFirst() = this.limit(1).first()
fun Query.fetchFirstOrNull() = this.limit(1).firstOrNull()
fun <T> Query.fetchFirst(exp: Expression<T>) = this.fetchFirst()[exp]
fun <T> Query.fetchFirstOrNull(exp: Expression<T>) = this.fetchFirstOrNull()?.get(exp)


fun Query.exists(): Boolean = Table.Dual.select(intLiteral(1))
    .where(exists(this@exists))
    .count() != 0L