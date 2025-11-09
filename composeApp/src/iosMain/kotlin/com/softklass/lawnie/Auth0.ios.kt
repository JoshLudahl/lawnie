package com.softklass.lawnie

import platform.Foundation.NSBundle

actual fun auth0ClientId(): String {
    val dict = NSBundle.mainBundle.infoDictionary
    val value = dict?.get("AUTH0_CLIENT_ID") as? String
    return value ?: ""
}
