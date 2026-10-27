package com.availelabs.tabla.ingestion.source

import java.net.URI

/** Raw source content; its schema is validated when parsed. */
interface SourceFile {
    val bytes: ByteArray
}

/** Binds a raw document type to the URI used to download and store it. */
class SnapshotSource<T : SourceFile>(
    val uri: URI,
    val wrap: (ByteArray) -> T
)
