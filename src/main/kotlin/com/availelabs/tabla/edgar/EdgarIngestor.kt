package com.availelabs.tabla.edgar

import org.springframework.stereotype.Component

@Component
class EdgarIngestor(
    private val properties: EdgarProperties
)
