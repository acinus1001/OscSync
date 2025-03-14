package dev.kuro9.domain.database

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@ImportAutoConfiguration(
    value = [ExposedAutoConfiguration::class],
    exclude = [DataSourceTransactionManagerAutoConfiguration::class]
)
@EnableTransactionManagement
class ExposedConfig {

    @Bean
    fun exposedDatabaseConfig(): DatabaseConfig = DatabaseConfig {
        useNestedTransactions = true
    }

    @[Primary Bean]
    fun exposedRWDatabase(dataSource: DataSource): Database =
        Database.connect(dataSource, databaseConfig = exposedDatabaseConfig())
}