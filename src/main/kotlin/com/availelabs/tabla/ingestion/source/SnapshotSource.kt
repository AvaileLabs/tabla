package com.availelabs.tabla.ingestion.source

import kotlinx.io.bytestring.ByteString
import java.net.URI

/** Immutable raw source content; its schema is validated when parsed. */
interface SourceFile {
    val bytes: ByteString
}

/** Binds a raw document type to the URI used to download and store it. */
value class SnapshotSource<T : SourceFile>(
    val uri: URI,
    val wrap: (ByteString) -> T
)
