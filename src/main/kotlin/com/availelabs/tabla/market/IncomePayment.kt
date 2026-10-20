package com.availelabs.tabla.market

import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.util.*

sealed interface IncomePayment

data class CashDividend(
    val amount: BigDecimal,
    val currency: Currency,
    val paymentDate: LocalDate
) : IncomePayment
