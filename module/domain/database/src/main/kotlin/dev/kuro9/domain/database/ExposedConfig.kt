package dev.kuro9.domain.database

import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.spring.boot.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.v1.spring.transaction.SpringTransactionManager
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

    @[Primary Bean("transactionManager")]
    fun transactionManager(txManager: SpringTransactionManager) = txManager

    @Bean
    fun exposedDatabaseConfig(): DatabaseConfig = DatabaseConfig {
        useNestedTransactions = true
    }

    @[Primary Bean]
    fun exposedRWDatabase(dataSource: DataSource): Database =
        Database.connect(dataSource, databaseConfig = exposedDatabaseConfig())
}