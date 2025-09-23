package com.softklass.lawnie

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

private val httpClient = HttpClient {
    install(ContentNegotiation) { json() }
}

@Serializable
private data class SaveZoneReq(val deviceId: String, val zoneCode: String)

suspend fun apiGetZones(): List<Zone> {
    val url = serverBaseUrl().trimEnd('/') + "/zones"
    return httpClient.get(url).body()
}

suspend fun apiSaveUserZone(deviceId: String, zoneCode: String): Boolean {
    val url = serverBaseUrl().trimEnd('/') + "/user/zone"
    val resp = httpClient.post(url) {
        contentType(ContentType.Application.Json)
        setBody(SaveZoneReq(deviceId = deviceId, zoneCode = zoneCode))
    }
    return resp.status.value in 200..299
}
