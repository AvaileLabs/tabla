package com.availelabs.tabla.ingestion.source

import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlin.time.Instant

/**
 * Attaches source metadata to raw bytes, wrapping them in a source-specific
 * type so parameters and return types distinguish the source documents.
 */
fun <T : SourceFile> ByteArray.sourcedBy(
    source: SnapshotSource<T>,
    retrievedAt: Instant,
    createdAt: SourceTimestamp? = null
): Sourced<T> =
    source.wrap(this).sourcedBy(
        uri = source.uri,
        retrievedAt = retrievedAt,
        createdAt = createdAt
    )
