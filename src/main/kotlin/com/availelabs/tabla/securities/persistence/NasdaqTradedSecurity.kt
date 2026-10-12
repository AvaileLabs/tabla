package com.availelabs.tabla.securities.persistence

import org.babyfish.jimmer.sql.Embeddable
import org.jetbrains.kotlinx.dataframe.annotations.ColumnName
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
@Embeddable
interface NasdaqTradedSecurity {
    @ColumnName("Nasdaq Traded")
    val nasdaqTraded: String

    @ColumnName("Symbol")
    val symbol: String

    @ColumnName("Security Name")
    val securityName: String

    @ColumnName("Listing Exchange")
    val listingExchange: Char

    @ColumnName("Market Category")
    val marketCategory: Char?

    @ColumnName("ETF")
    val etf: Char

    @ColumnName("Round Lot Size")
    val roundLotSize: Int

    @ColumnName("Test Issue")
    val testIssue: Char

    @ColumnName("Financial Status")
    val financialStatus: Char?

    @ColumnName("CQS Symbol")
    val cqsSymbol: String?

    @ColumnName("NASDAQ Symbol")
    val nasdaqSymbol: String

    @ColumnName("NextShares")
    val nextShares: Char
}