package com.availelabs.tabla.market.sec.company.internal

import org.babyfish.jimmer.sql.Embeddable

@Embeddable
interface SecCompanyTickerId {
    val cik: Int
    val symbol: String
}
