package com.availelabs.tabla.ingestion

import org.springframework.modulith.ApplicationModule

@ApplicationModule(allowedDependencies = ["provenance", "market"])
class IngestionModule