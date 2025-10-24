package dev.kuro9.application.test

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.stringLiteral
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TestService {

    fun test() {
        Table.Dual.select(stringLiteral("testStr")).first()[stringLiteral("testStr")]
            .also { println(it) }
    }
}