package com.example.ftxtrademanager.model

data class Order(
        val id: Number,
        val market: String,
        val type: String,
        val side: String,
        val price: Number,
        val size: Number,
        val filledSize: Number,
        val remainingSize: Number,
        val avgFillPrice: Number?,
        val status: String,
        val createdAt: String,
        val reduceOnly: Boolean,
        val ioc: Boolean,
        val postOnly: Boolean,
        val clientId: String?
)
