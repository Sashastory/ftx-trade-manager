package com.example.ftxtrademanager.model

data class Position(
        val cost: Number,
        val entryPrice: Number?,
        val estimatedLiquidationPrice: Number?,
        val future: String,
        val initialMarginRequirement: Number,
        val longOrderSize: Number,
        val maintenanceMarginRequirement: Number,
        val netSize: Number,
        val openSize: Number,
        val realizedPnl: Number,
        val shortOrderSize: Number,
        val side: String,
        val size: Number,
        val unrealizedPnl: Number,
        val collateralUsed: Number
)
