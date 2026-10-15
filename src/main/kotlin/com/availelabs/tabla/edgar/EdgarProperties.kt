package com.availelabs.tabla.edgar

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration for SEC EDGAR requests.
 *
 * @property userAgent HTTP User-Agent identifying the application and
 * including a valid contact email (e.g., "Tabla contact@example.com").
 */
@ConfigurationProperties(prefix = "tabla.edgar")
data class EdgarProperties(
    val userAgent: String
)
