package dev.kuro9.application.homepage.config

import kotlinx.datetime.LocalDate
import org.springframework.core.convert.converter.Converter

class StringToKotlinxLocalDateConverter : Converter<String, LocalDate> {
    override fun convert(source: String): LocalDate? {
        if (source.isBlank()) return null

        return try {
            LocalDate.parse(source)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("올바르지 않은 날짜 형식입니다.", e)
        }
    }
}