package dev.kuro9.domain.smartapp.user

import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableTransactionManagement
@Configuration(proxyBeanMethods = false)
class SmartAppDatabaseConfig