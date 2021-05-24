package com.example.ftxtrademanager.model

data class Order(
        val id: Long,
        val market: String,
        val type: String,
        val side: String,
        val price: Double,
        val size: Double,
        val filledSize: Double,
        val remainingSize: Double,
        val avgFillPrice: Double?,
        val status: String,
        val createdAt: String,
        val reduceOnly: Boolean,
        val ioc: Boolean,
        val postOnly: Boolean,
        val clientId: String?
)
