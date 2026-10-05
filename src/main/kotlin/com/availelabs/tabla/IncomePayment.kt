package com.availelabs.tabla

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

sealed interface IncomePayment

data class CashDividend(
    val amount: BigDecimal,
    val currency: Currency,
    val paymentDate: LocalDate
) : IncomePayment