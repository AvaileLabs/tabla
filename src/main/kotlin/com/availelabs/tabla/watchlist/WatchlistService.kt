package com.availelabs.tabla.watchlist

import com.availelabs.tabla.Stock
import com.availelabs.tabla.StockId
import org.springframework.stereotype.Service

@Service
class WatchlistService {
    // temporary - later will become repository call
    private val watchlist = StockWatchlist()

    fun addStock(stock: Stock): StockWatchlist.AddStockResult {
        return watchlist.add(stock)
    }

    fun removeStock(stockId: StockId): StockWatchlist.RemoveStockResult {
        return watchlist.remove(stockId)
    }
}