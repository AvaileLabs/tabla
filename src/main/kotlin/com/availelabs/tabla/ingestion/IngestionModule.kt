package com.availelabs.tabla.ingestion

import org.springframework.modulith.ApplicationModule

@ApplicationModule(
    allowedDependencies = ["provenance", "market :: nasdaq", "market :: sec-companies", "market :: sec-funds"]
)
class IngestionModule
