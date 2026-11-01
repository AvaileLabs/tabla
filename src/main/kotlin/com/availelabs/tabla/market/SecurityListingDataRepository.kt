package com.availelabs.tabla.market

import com.availelabs.tabla.market.internal.SecurityListingEntity
import com.availelabs.tabla.market.internal.dto.SecurityListingInput
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SecurityListingDataRepository(
    private val sqlClient: KSqlClient
) {
    @Transactional
    fun saveAll(
        securityListings: ImmutableList<SecurityListing>
    ) {
        require(securityListings.isNotEmpty()) { "Security listings must not be empty" }

        require(securityListings.distinctBy { it.symbols.nasdaqSymbol }
            .size == securityListings.size) { "Security listings must have distinct NASDAQ symbols" }

        val inputs = securityListings.map { data ->
            SecurityListingInput(
                cik = data.cik,
                symbol = data.symbols.symbol,
                nasdaqSymbol = data.symbols.nasdaqSymbol,
                cqsSymbol = data.symbols.cqsSymbol,
                edgarSymbol = data.symbols.edgarSymbol,
                name = data.name,
                exchange = data.exchange
            )
        }

        sqlClient.createDelete(SecurityListingEntity::class) {}.execute()
        sqlClient.saveInputs(inputs, SaveMode.INSERT_ONLY)
    }
}
