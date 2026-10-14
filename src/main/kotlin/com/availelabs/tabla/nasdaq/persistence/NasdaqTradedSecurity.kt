package com.availelabs.tabla.nasdaq.persistence

import org.babyfish.jimmer.sql.Embeddable
import org.jetbrains.kotlinx.dataframe.annotations.ColumnName
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

/* The reason we don't include testIssue in NasdaqTradedSecurity is because @Embeddable is used to tell
Jimmer ORM to map properties to database columns. The testIssue is needed for dataframe filtering in the
NasdaqSecurityDirectoryIngestor, but it's not persisted in the database.
 */
@DataSchema
interface NasdaqTestIssueSchema {

    @ColumnName("Test Issue")
    val testIssue: Char
}

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

    @ColumnName("Financial Status")
    val financialStatus: Char?

    @ColumnName("CQS Symbol")
    val cqsSymbol: String?

    @ColumnName("NASDAQ Symbol")
    val nasdaqSymbol: String

    @ColumnName("NextShares")
    val nextShares: Char
}