package com.availelabs.tabla

import java.net.URI

object SourceCatalog {
    val nasdaqDirectory: URI = URI.create("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqtraded.txt")
    val edgarCompanyTickers: URI = URI.create("https://www.sec.gov/files/company_tickers_exchange.json")
    val edgarMutualFundTickers: URI = URI.create("https://www.sec.gov/files/company_tickers_mf.json")
}