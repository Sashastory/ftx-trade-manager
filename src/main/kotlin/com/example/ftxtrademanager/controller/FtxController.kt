package com.example.ftxtrademanager.controller

import com.example.ftxtrademanager.hmac.HmacSha256SignatureWriter
import com.example.ftxtrademanager.model.BalanceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class FtxController(
        val restTemplate: RestTemplate,
        val signatureWriter: HmacSha256SignatureWriter
) {
    @Value("\${ftx.url}")
    val url: String = ""

    @Value("\${ftx.api-key}")
    val apiKey: String = ""

    @GetMapping("balances")
    fun getBalances(): ResponseEntity<BalanceResponse> {
        val balancePath = "/api/wallet/balances"
        val ts = System.currentTimeMillis().toString()
        val headers = HttpHeaders().apply {
            set("FTX-KEY", apiKey)
            set("FTX-TS", ts)
            set("FTX-SIGN", signatureWriter.writeSignature(ts, HttpMethod.GET, balancePath))
        }
        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        return restTemplate.exchange("${url}${balancePath}", HttpMethod.GET, request, BalanceResponse::class.java)
    }
}
