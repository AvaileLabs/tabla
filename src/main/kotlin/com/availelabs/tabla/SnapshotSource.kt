package com.availelabs.tabla

import java.net.URI

/** Raw source content; its schema is validated when parsed. */
interface SourceFile {
    val bytes: ByteArray
}

/** Binds a raw document type to the URI used to download and store it. */
class SnapshotSource<T : SourceFile> internal constructor(
    val uri: URI,
    internal val wrap: (ByteArray) -> T
)
