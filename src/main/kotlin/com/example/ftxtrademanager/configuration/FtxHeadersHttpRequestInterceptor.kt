package com.example.ftxtrademanager.configuration

import org.apache.commons.codec.binary.Hex
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.vault.core.VaultKeyValueOperationsSupport
import org.springframework.vault.core.VaultTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class FtxHeadersHttpRequestInterceptor(
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
