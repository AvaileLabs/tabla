package com.availelabs.tabla.edgar

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration for SEC EDGAR requests.
 *
 * @property userAgent HTTP User-Agent identifying the application and
 * including a valid contact email (e.g., "Tabla contact@example.com").
 * Configured through `EDGAR_USER_AGENT` in application.yaml.
 */
@ConfigurationProperties(prefix = "tabla.edgar")
data class EdgarProperties(
    val userAgent: String
) {
    init {
        require(userAgent.isNotBlank()) {
            "SEC EDGAR User-Agent must be configured (EDGAR_USER_AGENT)"
        }
    }
}
