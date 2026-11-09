package com.availelabs.tabla.market.nasdaq.internal

import com.availelabs.tabla.market.nasdaq.ListingExchange
import kotlinx.datetime.LocalDateTime
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.net.URI
import kotlin.time.Instant

@Entity
@Table(name = "nasdaq_listing")
interface NasdaqDirectoryEntryEntity {
    val symbol: String

    @Id
    val nasdaqSymbol: String
    val cqsSymbol: String?
    val name: String
    val exchange: ListingExchange
    val nasdaqTraded: Boolean
    val etf: Boolean
    val roundLotSize: Int
    val marketCategory: Char?
    val financialStatus: Char?
    val nextShares: Boolean
    val sourceUri: URI
    val retrievedAt: Instant
    val sourceCreatedAt: LocalDateTime?
}
