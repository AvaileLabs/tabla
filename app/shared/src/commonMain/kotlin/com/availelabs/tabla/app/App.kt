package com.availelabs.tabla.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.*
import kotlin.uuid.Uuid

@Composable
fun App() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText("Tabla")
    }
}

value class Watchlist private constructor(
    val securities: PersistentList<Security>,
) {
    fun addSecurity(security: Security): Watchlist =
        Watchlist(securities + security)

    fun removeSecurity(security: Security): Watchlist =
        Watchlist(securities - security)

    companion {
        fun empty(): Watchlist =
            Watchlist(persistentListOf())

        fun of(vararg securities: Security): Watchlist =
            Watchlist(persistentListOf(*securities))

        fun from(securities: Iterable<Security>): Watchlist =
            Watchlist(securities.toPersistentList())
    }
}

value class Security private constructor(
    val id: Uuid,
    val name: String,
    val exchange: String
) {
    init {
        require(name.isNotBlank()) { "Security name cannot be blank" }
        require(exchange.isNotBlank()) { "Security exchange cannot be blank" }
    }
}