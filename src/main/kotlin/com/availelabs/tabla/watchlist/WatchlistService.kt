package com.availelabs.tabla.watchlist

import com.availelabs.tabla.Stock
import org.springframework.stereotype.Service

@Service
class WatchlistService {
    // temporary - later will become repository call
    private val watchlist = StockWatchlist()

    fun addStock(stock: Stock): StockWatchlist.AddStockResult {
        return watchlist.add(stock)
    }

    fun removeStock(stock: Stock): StockWatchlist.RemoveStockResult {
        return watchlist.remove(stock)
    }
}