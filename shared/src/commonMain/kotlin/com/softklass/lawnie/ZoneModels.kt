package com.softklass.lawnie

import kotlinx.serialization.Serializable

@Serializable
data class Zone(
    val code: String,
    val name: String
)

@Serializable
data class SaveZoneRequest(
    val deviceId: String,
    val zoneCode: String
)
