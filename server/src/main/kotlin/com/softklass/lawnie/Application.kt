package com.softklass.lawnie

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

private object Db {
    private const val JDBC_URL = "jdbc:sqlite:lawnie.db"
    val connection: Connection by lazy {
        val conn = DriverManager.getConnection(JDBC_URL)
        conn.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS user_zones (
                    device_id TEXT PRIMARY KEY,
                    zone_code TEXT NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        conn
    }

    fun upsertUserZone(deviceId: String, zoneCode: String) {
        val now = Instant.now().epochSecond
        connection.prepareStatement(
            "INSERT INTO user_zones(device_id, zone_code, updated_at) VALUES(?,?,?) " +
                "ON CONFLICT(device_id) DO UPDATE SET zone_code=excluded.zone_code, updated_at=excluded.updated_at"
        ).use { ps ->
            ps.setString(1, deviceId)
            ps.setString(2, zoneCode)
            ps.setLong(3, now)
            ps.executeUpdate()
        }
    }
}

@Serializable
private data class SaveZoneReq(val deviceId: String, val zoneCode: String)

fun Application.module() {
    install(ContentNegotiation) { json() }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        // Returns the latest USDA Plant Hardiness Zones list (2023 update)
        get("/zones") {
            val zones = latestZones()
            call.respond(zones)
        }
        post("/user/zone") {
            val body = call.receive<SaveZoneReq>()
            // Basic validation: ensure zone exists
            val valid = latestZones().any { it.code == body.zoneCode }
            if (!valid) {
                call.respondText("Invalid zone", status = io.ktor.http.HttpStatusCode.BadRequest)
            } else {
                Db.upsertUserZone(body.deviceId, body.zoneCode)
                call.respondText("Saved", status = io.ktor.http.HttpStatusCode.OK)
            }
        }
    }
}

@Serializable
private data class ZoneDto(val code: String, val name: String)

private fun latestZones(): List<ZoneDto> {
    // USDA Plant Hardiness Zones updated in 2023; zones 1A–13B
    // We'll provide zone codes and human-friendly names.
    val codes = listOf(
        "1A","1B","2A","2B","3A","3B","4A","4B","5A","5B",
        "6A","6B","7A","7B","8A","8B","9A","9B","10A","10B",
        "11A","11B","12A","12B","13A","13B"
    )
    return codes.map { code ->
        val name = "Zone $code"
        ZoneDto(code = code, name = name)
    }
}