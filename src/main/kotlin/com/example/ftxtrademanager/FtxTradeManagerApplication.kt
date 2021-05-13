package com.example.ftxtrademanager

import org.apache.commons.codec.binary.Hex
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.vault.core.VaultKeyValueOperationsSupport
import org.springframework.vault.core.VaultTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@SpringBootApplication
class FtxTradeManagerApplication {
    @Bean
    fun restTemplate(interceptor: CustomHttpRequestInterceptor): RestTemplate {
        return RestTemplateBuilder()
                .additionalInterceptors(interceptor)
                .build()
    }
}

fun main(args: Array<String>) {
    runApplication<FtxTradeManagerApplication>(*args)
}

@Component
class CustomHttpRequestInterceptor(
        val vaultTemplate: VaultTemplate
) : ClientHttpRequestInterceptor {
    private val BALANCE_PATH = "/api/wallet/balances"
    private val API_KEY = "6VGOx6WqwH1kTMsSXTqTM_3oR3N352V45Rfdg3rQ"

    override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val sha256Mac = Mac.getInstance("HmacSHA256")
        val vaultResponse = vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2)
                .get("ftx-trade-manager")
        var apiSecret = ""
        vaultResponse?.let { vr ->
            vr.data?.let { data ->
                apiSecret = data["api-secret"].toString()
            }
        } ?: throw RuntimeException("No api secret present in vault")
        val secretKey = SecretKeySpec(apiSecret.toByteArray(), "HmacSHA256")
        val ts = System.currentTimeMillis().toString()
        sha256Mac.init(secretKey)
        val signatureByteArray: ByteArray = "${ts}GET${BALANCE_PATH}".toByteArray()
        val signature: String = Hex.encodeHexString(sha256Mac.doFinal(signatureByteArray))
        request.headers.apply {
            set("FTX-KEY", API_KEY)
            set("FTX-TS", ts)
            set("FTX-SIGN", signature)
        }
        return execution.execute(request, body)
    }

}

@RestController
class FtxController(
        val restTemplate: RestTemplate
) {
    private val URL = "https://ftx.com/api/wallet/balances"

    @GetMapping("balances")
    fun getBalances(): ResponseEntity<BalanceResponse> = restTemplate.getForEntity(
            URL,
            BalanceResponse::class.java
    )
}

data class Balance(val coin: String, val free: Number, val total: Number)

data class BalanceResponse(val success: Boolean, val result: List<Balance>)
