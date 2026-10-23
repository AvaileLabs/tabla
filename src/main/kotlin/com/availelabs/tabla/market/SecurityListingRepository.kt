package com.availelabs.tabla.market

import com.availelabs.tabla.market.internal.dto.SecurityListingInput
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
class SecurityListingRepository(
    private val sqlClient: KSqlClient
) {
    @OptIn(ExperimentalUuidApi::class)
    fun save(securityListings: ImmutableList<SecurityListing>) {
        for (listing in securityListings) {
            sqlClient.save(
                SecurityListingInput(
                    id = Uuid.generateV7(),
                    cik = listing.cik,
                    symbol = listing.symbols.symbol,
                    nasdaqSymbol = listing.symbols.nasdaqSymbol,
                    cqsSymbol = listing.symbols.cqsSymbol,
                    exchange = listing.exchange
                )
            )
        }
    }
}
