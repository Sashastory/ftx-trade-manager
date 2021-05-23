package com.example.ftxtrademanager.controller

import com.example.ftxtrademanager.hmac.HmacSha256SignatureWriter
import com.example.ftxtrademanager.model.ApiResponse
import com.example.ftxtrademanager.model.Balance
import com.example.ftxtrademanager.model.Order
import com.example.ftxtrademanager.model.Position
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@RestController
class FtxController(
    val restTemplate: RestTemplate,
    val signatureWriter: HmacSha256SignatureWriter
) {
    private val ftxKeyHeader: String = "FTX-KEY"
    private val ftxTsHeader: String = "FTX-TS"
    private val ftxSignHeader: String = "FTX-SIGN"

    @Value("\${ftx.url}")
    val url: String = ""

    @Value("\${ftx.api-key}")
    val apiKey: String = ""

    @GetMapping("balances")
    fun getBalances(): ResponseEntity<ApiResponse<Balance>> {
        val balancePath = "/api/wallet/balances"
        return doGetRequest(balancePath, null)
    }

    @GetMapping("open-orders")
    fun getOpenOrders(
        @RequestParam("market", required = false) market: String?
    ): ResponseEntity<ApiResponse<Order>> {
        val openOrdersPath = "/api/orders"
        val uriVariables = mutableMapOf<String, String>()
        market?.let { uriVariables.put("market", market) }
        return doGetRequest(openOrdersPath, uriVariables)
    }

    @GetMapping("positions")
    fun getPositions(
        @RequestParam("showAvgPrice", required = false) showAvgPrice: Boolean?
    ): ResponseEntity<ApiResponse<Position>> {
        val positionsPath = "/api/positions"
        val uriVariables = mutableMapOf<String, String>()
        showAvgPrice?.let { uriVariables.put("showAvgPrice", showAvgPrice.toString()) }
        return doGetRequest(positionsPath, uriVariables)
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
        return doGetRequest(orderHistoryPath, uriVariables)
    }

    private fun <R> doGetRequest(
        path: String,
        uriVariables: MutableMap<String, String>?
    ): ResponseEntity<ApiResponse<R>> {
        val ts = System.currentTimeMillis().toString()
        val builder = UriComponentsBuilder.fromHttpUrl("${url}${path}")
        uriVariables?.let {
            uriVariables.forEach { entry -> builder.queryParam(entry.key, entry.value) }
        }
        val headers = HttpHeaders().apply {
            set(ftxKeyHeader, apiKey)
            set(ftxTsHeader, ts)
            set(
                ftxSignHeader,
                signatureWriter.writeSignature(ts, HttpMethod.GET, builder.toUriString().substringAfter(url))
            )
        }
        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        return restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.GET,
            request,
            ParameterizedTypeReference.forType(ApiResponse::class.java)
        )
    }
}
