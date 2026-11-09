package com.availelabs.tabla.market.sec.company.internal

import com.availelabs.tabla.market.sec.company.SecCompanyTicker
import com.availelabs.tabla.market.sec.company.SecCompanyTickerWriter
import com.availelabs.tabla.market.sec.company.internal.dto.SecCompanyTickerInput
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SecCompanyTickerRepository(private val sqlClient: KSqlClient) : SecCompanyTickerWriter {
    @Transactional
    override fun replaceAll(snapshot: Sourced<ImmutableList<SecCompanyTicker>>) {
        val companies = snapshot.value
        require(companies.isNotEmpty()) { "Company tickers must not be empty" }
        require(companies.distinctBy { it.cik to it.symbol }.size == companies.size) {
            "Company tickers must have distinct CIK and symbol pairs"
        }

        val source = snapshot.sourceInfo
        val inputs = companies.map { company ->
            SecCompanyTickerInput(
                id = SecCompanyTickerId {
                    cik = company.cik
                    symbol = company.symbol
                },
                name = company.name,
                exchange = company.exchange,
                sourceUri = source.uri,
                retrievedAt = source.retrievedAt
            )
        }

        sqlClient.createDelete(SecCompanyTickerEntity::class) {}.execute()
        sqlClient.saveInputs(inputs, SaveMode.INSERT_ONLY)
    }
}
