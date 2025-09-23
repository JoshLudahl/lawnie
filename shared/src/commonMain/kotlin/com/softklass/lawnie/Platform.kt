package com.softklass.lawnie

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform