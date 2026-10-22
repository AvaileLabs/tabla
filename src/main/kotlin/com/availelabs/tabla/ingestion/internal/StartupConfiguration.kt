package com.availelabs.tabla.ingestion.internal

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class StartupConfiguration {

    @Bean
    fun initialIngestion(pipeline: IngestionPipeline): ApplicationRunner =
        ApplicationRunner { pipeline.run() }
}
