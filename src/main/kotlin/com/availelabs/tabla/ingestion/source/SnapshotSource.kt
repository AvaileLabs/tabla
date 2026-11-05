package com.availelabs.tabla.ingestion.source

import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlinx.io.bytestring.ByteString
import java.net.URI
import kotlin.time.Instant

/** Immutable raw source content; its schema is validated when parsed. */
interface SourceFile {
    val bytes: ByteString
}

/** Binds a raw document type to the URI used to download and store it. */
value class SnapshotSource<T : SourceFile>(
    val uri: URI,
    val wrap: (ByteString) -> T
)

/** Wraps immutable bytes in a source-specific document type and attaches source metadata. */
fun <T : SourceFile> ByteString.sourcedBy(
    source: SnapshotSource<T>,
    retrievedAt: Instant,
    createdAt: SourceTimestamp? = null
): Sourced<T> =
    source.wrap(this).sourcedBy(
        uri = source.uri,
        retrievedAt = retrievedAt,
        createdAt = createdAt
    )
