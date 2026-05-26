package com.trip.dayplan

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
