package com.availelabs.tabla.ingestion.edgar

import kotlinx.datetime.Month
import java.net.URI

enum class CalendarQuarter(val number: Int) {
    Q1(1),
    Q2(2),
    Q3(3),
    Q4(4);

    companion {
        fun fromMonth(month: Month): CalendarQuarter =
            entries[month.ordinal / 3]
    }
}

value class EdgarQuarter(
    val quarter: CalendarQuarter,
    val year: Int,
) {
    init {
        require(year >= 1993) {
            "EDGAR quarterly index archives do not extend before 1993"
        }
    }

    val indexUri: URI
        get() = URI.create(
            "https://www.sec.gov/Archives/edgar/full-index/$year/QTR${quarter.number}/master.gz"
        )
}
