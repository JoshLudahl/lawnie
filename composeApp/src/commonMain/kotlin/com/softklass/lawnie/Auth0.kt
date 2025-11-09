package com.softklass.lawnie

object Auth0Config {
    const val DOMAIN: String = "dev-wi4a57yezfutqnp2.us.auth0.com"
    // CLIENT_ID moved to per-platform properties; see `auth0ClientId()`
    const val REDIRECT_URI: String = "com.softklass.lawnie://dev-wi4a57yezfutqnp2.us.auth0.com/android/com.softklass.lawnie/callback"
    const val SCOPE: String = "openid%20profile%20email" // already URL-encoded space
    const val RESPONSE_TYPE: String = "code"
    // Optional: set audience if you have an API configured. Leave empty to omit.
    const val AUDIENCE: String = "https://dev-wi4a57yezfutqnp2.us.auth0.com/api/v2/"
}

// Expect/actual provider for Auth0 client id (from untracked per-platform config)
expect fun auth0ClientId(): String

fun auth0AuthorizeUrl(): String {
    val base = "https://${Auth0Config.DOMAIN}/authorize"
    val commonParams = "response_type=${Auth0Config.RESPONSE_TYPE}" +
        "&client_id=${auth0ClientId()}" +
        "&redirect_uri=${Auth0Config.REDIRECT_URI}" +
        "&scope=${Auth0Config.SCOPE}"
    val audienceParam = if (Auth0Config.AUDIENCE.isNotBlank()) "&audience=${Auth0Config.AUDIENCE}" else ""
    return "$base?$commonParams$audienceParam"
}
