package com.availelabs.tabla.market.sec.fund.internal

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.net.URI
import kotlin.time.Instant

@Entity
@Table(name = "fund_share_class")
interface FundShareClassEntity {
    @Id
    val classId: String
    val cik: Int
    val seriesId: String
    val symbol: String?
    val sourceUri: URI
    val retrievedAt: Instant
}
