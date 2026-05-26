package com.trip.dayplan.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.trip.dayplan.db.DayPlanDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File(System.getProperty("user.home"), ".dayplan/dayplan.db")
        dbFile.parentFile?.mkdirs()

        val currentVersion = 1
        val versionFile = File(dbFile.parentFile, ".schema_version")
        val needsRecreate = !dbFile.exists() ||
            !versionFile.exists() ||
            versionFile.readText().trim() != currentVersion.toString()

        if (needsRecreate && dbFile.exists()) {
            dbFile.delete()
            File("${dbFile.absolutePath}-journal").delete()
            File("${dbFile.absolutePath}-wal").delete()
            File("${dbFile.absolutePath}-shm").delete()
        }

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        DayPlanDatabase.Schema.create(driver)
        versionFile.writeText(currentVersion.toString())
        return driver
    }
}
