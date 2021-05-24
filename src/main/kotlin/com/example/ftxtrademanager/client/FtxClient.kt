package com.example.ftxtrademanager.client

import com.example.ftxtrademanager.hmac.HmacSha256SignatureWriter
import com.example.ftxtrademanager.model.ApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class FtxClient(
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

    fun <R> doGetRequest(
        path: String,
        uriVariables: MutableMap<String, String>?,
        type: ParameterizedTypeReference<ApiResponse<R>>
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
            type
        )
    }
}
