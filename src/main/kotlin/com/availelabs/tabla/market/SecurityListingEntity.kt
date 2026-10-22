package com.availelabs.tabla.market

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import kotlin.uuid.Uuid

@Entity
@Table(name = "security_listing")
interface SecurityListingEntity {
    @Id
    val id: Uuid
    val cik: Int
    val symbol: String
    val nasdaqSymbol: String
    val cqsSymbol: String?
    val exchange: Exchange
}
