package com.availelabs.tabla.ingestion.reconciliation

import com.availelabs.tabla.market.SecurityListing
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository

@Repository
class EdgarNasdaqListingRepository(
    private val sqlClient: KSqlClient
) {
    fun insertSecurities(listings: ImmutableList<SecurityListing>) {

    }
}