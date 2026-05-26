package com.trip.dayplan

import com.trip.dayplan.data.DatabaseDriverFactory

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
