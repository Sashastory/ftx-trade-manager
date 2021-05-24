package com.example.ftxtrademanager.model

data class Position(
        val cost: Double,
        val entryPrice: Double?,
        val estimatedLiquidationPrice: Double?,
        val future: String,
        val initialMarginRequirement: Double,
        val longOrderSize: Double,
        val maintenanceMarginRequirement: Double,
        val netSize: Double,
        val openSize: Double,
        val realizedPnl: Double,
        val shortOrderSize: Double,
        val side: String,
        val size: Double,
        val unrealizedPnl: Double,
        val collateralUsed: Double
)
