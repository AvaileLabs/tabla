package com.availelabs.tabla.market.internal

import com.availelabs.tabla.market.Exchange
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "security_listing")
interface SecurityListingEntity {
    val cik: Int
    val symbol: String

    @Id
    val nasdaqSymbol: String
    val cqsSymbol: String?
    val edgarSymbol: String
    val name: String
    val exchange: Exchange
}
