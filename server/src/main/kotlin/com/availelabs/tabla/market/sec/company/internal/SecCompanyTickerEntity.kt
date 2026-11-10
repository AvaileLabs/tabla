package com.availelabs.tabla.market.sec.company.internal

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.net.URI
import kotlin.time.Instant

@Entity
@Table(name = "company_ticker")
interface SecCompanyTickerEntity {
    @Id
    val id: SecCompanyTickerId
    val name: String
    val exchange: String?
    val sourceUri: URI
    val retrievedAt: Instant
}
