package com.example.ftxtrademanager.hmac

import org.apache.commons.codec.binary.Hex
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.vault.core.VaultKeyValueOperationsSupport
import org.springframework.vault.core.VaultTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HmacSha256SignatureWriter(
        val vaultTemplate: VaultTemplate
) {
    private val ALGORITHM: String = "HmacSHA256"

    fun writeSignature(timestamp: String, method: HttpMethod, path: String): String {
        val sha256Mac = Mac.getInstance(ALGORITHM)
        val vaultResponse = vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2)
                .get("ftx-trade-manager")
        var apiSecret = ""
        vaultResponse?.let { vr ->
            vr.data?.let { data ->
                apiSecret = data["api-secret"].toString()
            }
        } ?: throw RuntimeException("No api secret present in vault")
        val secretKey = SecretKeySpec(apiSecret.toByteArray(), ALGORITHM)
        sha256Mac.init(secretKey)
        val signatureByteArray: ByteArray = "${timestamp}${method.name}${path}".toByteArray()
        return Hex.encodeHexString(sha256Mac.doFinal(signatureByteArray))
    }
}
