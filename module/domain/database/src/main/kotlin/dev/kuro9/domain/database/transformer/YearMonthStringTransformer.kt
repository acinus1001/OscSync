package dev.kuro9.domain.database.transformer

import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import org.jetbrains.exposed.v1.core.ColumnTransformer

object YearMonthStringTransformer : ColumnTransformer<String, YearMonth> {
    private val formatter = YearMonth.Format {
        year(); monthNumber();
    }

    override fun unwrap(value: YearMonth): String {
        return "${value.year}${value.month.number.toString().padStart(2, '0')}"
    }

    override fun wrap(value: String): YearMonth {
        return YearMonth.parse(value, formatter)
    }
}