package com.availelabs.tabla.market.sec.company

import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList

interface SecCompanyTickerWriter {
    /** Replaces the complete SEC company ticker mapping atomically, retaining the snapshot's provenance. */
    fun replaceAll(snapshot: Sourced<ImmutableList<SecCompanyTicker>>)
}
