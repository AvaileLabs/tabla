package com.availelabs.tabla.market.sec.fund

import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList

interface FundShareClassWriter {
    /** Replaces the complete SEC fund share class mapping atomically, retaining the snapshot's provenance. */
    fun replaceAll(snapshot: Sourced<ImmutableList<FundShareClass>>)
}
