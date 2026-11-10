package com.availelabs.tabla.ingestion.internal.source

import java.net.URI

object SourceCatalog {
    val nasdaqDirectory = SnapshotSource(
        uri = URI.create("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqtraded.txt"),
        wrap = ::NasdaqDirectoryRawFile
    )
    val edgarCompanyTickers = SnapshotSource(
        uri = URI.create("https://www.sec.gov/files/company_tickers_exchange.json"),
        wrap = ::EdgarCompanyTickersRawFile
    )
    val edgarMutualFundTickers = SnapshotSource(
        uri = URI.create("https://www.sec.gov/files/company_tickers_mf.json"),
        wrap = ::EdgarFundTickersRawFile
    )
}
