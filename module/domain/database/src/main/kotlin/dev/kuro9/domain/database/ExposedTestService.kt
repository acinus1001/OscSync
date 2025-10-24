package dev.kuro9.domain.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ExposedTestService {

    fun testDatabase() {
        check(Table.Dual.select(intLiteral(1)).first()[intLiteral(1)] == 1)
    }
}