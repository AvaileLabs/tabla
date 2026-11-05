package com.availelabs.tabla.ingestion.source

import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlinx.io.bytestring.ByteString
import kotlin.time.Instant

/**
 * Copies raw bytes into an immutable snapshot and attaches source metadata,
 * wrapping them in a source-specific type to distinguish the source documents.
 */
fun <T : SourceFile> ByteArray.sourcedBy(
    source: SnapshotSource<T>,
    retrievedAt: Instant,
    createdAt: SourceTimestamp? = null
): Sourced<T> =
    source.wrap(ByteString(this)).sourcedBy(
        uri = source.uri,
        retrievedAt = retrievedAt,
        createdAt = createdAt
    )
