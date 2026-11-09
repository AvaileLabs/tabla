package com.availelabs.tabla.market.nasdaq

import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList

interface NasdaqDirectoryWriter {
    /** Replaces the complete Nasdaq directory atomically, retaining the snapshot's provenance. */
    fun replaceAll(snapshot: Sourced<ImmutableList<NasdaqDirectoryEntry>>)
}
