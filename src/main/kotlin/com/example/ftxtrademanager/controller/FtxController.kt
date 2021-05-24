package com.example.ftxtrademanager.controller

import com.example.ftxtrademanager.client.FtxClient
import com.example.ftxtrademanager.model.ApiResponse
import com.example.ftxtrademanager.model.Balance
import com.example.ftxtrademanager.model.Order
import com.example.ftxtrademanager.model.Position
import com.example.ftxtrademanager.typeReference
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FtxController(
    val ftxClient: FtxClient
) {
    @GetMapping("balances")
    fun getBalances(): ResponseEntity<ApiResponse<Balance>> {
        val balancePath = "/api/wallet/balances"
        return ftxClient.doGetRequest(balancePath, null, typeReference())
    }

    @GetMapping("open-orders")
    fun getOpenOrders(
        @RequestParam("market", required = false) market: String?
    ): ResponseEntity<ApiResponse<Order>> {
        val openOrdersPath = "/api/orders"
        val uriVariables = mutableMapOf<String, String>()
        market?.let { uriVariables.put("market", market) }
        return ftxClient.doGetRequest(openOrdersPath, uriVariables, typeReference())
    }

    @GetMapping("positions")
    fun getPositions(
        @RequestParam("showAvgPrice", required = false) showAvgPrice: Boolean?
    ): ResponseEntity<ApiResponse<Position>> {
        val positionsPath = "/api/positions"
        val uriVariables = mutableMapOf<String, String>()
        showAvgPrice?.let { uriVariables.put("showAvgPrice", showAvgPrice.toString()) }
        return ftxClient.doGetRequest(positionsPath, uriVariables, typeReference())
    }

    @GetMapping("order-history")
    fun getOrderHistory(
        @RequestParam("market", required = false) market: String?,
        @RequestParam("start_time", required = false) startTime: Number?,
        @RequestParam("end_time", required = false) endTime: Number?,
        @RequestParam("limit", required = false) limit: Number?
    ): ResponseEntity<ApiResponse<Order>> {
        val orderHistoryPath = "/api/orders/history"
        val uriVariables = mutableMapOf<String, String>()
        market?.let { uriVariables.put("market", market) }
        startTime?.let { uriVariables.put("start_time", startTime.toString()) }
        endTime?.let { uriVariables.put("end_time", endTime.toString()) }
        limit?.let { uriVariables.put("limit", limit.toString()) }
        return ftxClient.doGetRequest(orderHistoryPath, uriVariables, typeReference())
    }
}
