package com.availelabs.tabla.market

import com.availelabs.tabla.market.internal.SecurityListingEntity
import com.availelabs.tabla.market.internal.dto.SecurityListingInput
import com.availelabs.tabla.market.internal.nasdaqSymbol
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SecurityListingDataRepository(
    private val sqlClient: KSqlClient
) {
    @Transactional
    fun saveAll(
        securityListings: ImmutableList<SecurityListingData>
    ) {
        if (securityListings.isEmpty()) return

        require(securityListings.distinctBy { it.symbols.nasdaqSymbol }
            .size == securityListings.size) { "Security listings must have distinct NASDAQ symbols" }

        val existingByNasdaqSymbol = sqlClient
            .createQuery(SecurityListingEntity::class) {
                where(table.nasdaqSymbol valueIn securityListings.map { it.symbols.nasdaqSymbol })
                select(table)
            }
            .execute()
            .associateBy { it.nasdaqSymbol }

        val inputs = securityListings.map { data ->
            SecurityListingInput(
                id = existingByNasdaqSymbol[data.symbols.nasdaqSymbol]
                    ?.id
                    ?: SecurityListingId.generate().value,
                cik = data.cik,
                symbol = data.symbols.symbol,
                nasdaqSymbol = data.symbols.nasdaqSymbol,
                cqsSymbol = data.symbols.cqsSymbol,
                edgarSymbol = data.symbols.edgarSymbol,
                name = data.name,
                exchange = data.exchange
            )
        }

        sqlClient.saveInputs(inputs, SaveMode.UPSERT)
    }
}
