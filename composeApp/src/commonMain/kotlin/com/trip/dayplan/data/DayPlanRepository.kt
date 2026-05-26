package com.trip.dayplan.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.trip.dayplan.db.DayPlanDatabase
import com.trip.dayplan.domain.DaySchedule
import com.trip.dayplan.domain.DayTask
import com.trip.dayplan.domain.DefaultGroups
import com.trip.dayplan.domain.TaskGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DayPlanRepository(private val db: DayPlanDatabase) {
    private val q = db.dayPlanDatabaseQueries

    init {
        ensureDatabaseSeeded()
    }

    private fun ensureDatabaseSeeded() {
        val count = q.getGroupCount().executeAsOne()
        if (count == 0L) {
            DefaultGroups.groups.forEach { g ->
                q.insertGroup(g.name, g.colorHex, g.sortOrder.toLong())
            }
        }
    }

    // Groups
    fun observeGroups(): Flow<List<TaskGroup>> {
        return q.getAllGroups()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { TaskGroup(it.id, it.name, it.colorHex, it.sortOrder.toInt()) } }
    }

    suspend fun getGroups(): List<TaskGroup> = observeGroups().first()

    suspend fun insertGroup(name: String, colorHex: String, sortOrder: Int) {
        q.insertGroup(name, colorHex, sortOrder.toLong())
    }

    suspend fun updateGroup(group: TaskGroup) {
        q.updateGroup(group.name, group.colorHex, group.sortOrder.toLong(), group.id)
    }

    suspend fun deleteGroup(id: Long) {
        q.deleteGroup(id)
    }

    // Tasks
    fun observeTasksForDate(date: String): Flow<List<DayTask>> {
        return q.getTasksForDate(date)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { row ->
                DayTask(
                    id = row.id,
                    name = row.name,
                    description = row.description.orEmpty(),
                    durationMinutes = row.durationMinutes.toInt(),
                    groupId = row.groupId,
                    date = row.date,
                    startMinute = row.startMinute.toInt(),
                    isCompleted = row.isCompleted == 1L,
                    reminderMinutes = row.reminderMinutes.toInt(),
                    groupName = row.group_name,
                    groupColorHex = row.group_color_hex,
                )
            }}
    }

    suspend fun getTasksForDate(date: String): List<DayTask> =
        q.getTasksForDate(date).executeAsList().map { row ->
            DayTask(
                id = row.id,
                name = row.name,
                description = row.description.orEmpty(),
                durationMinutes = row.durationMinutes.toInt(),
                groupId = row.groupId,
                date = row.date,
                startMinute = row.startMinute.toInt(),
                isCompleted = row.isCompleted == 1L,
                reminderMinutes = row.reminderMinutes.toInt(),
                groupName = row.group_name,
                groupColorHex = row.group_color_hex,
            )
        }

    suspend fun getTaskById(id: Long): DayTask? {
        val row = q.getTaskById(id).executeAsOneOrNull() ?: return null
        return DayTask(
            id = row.id,
            name = row.name,
            description = row.description.orEmpty(),
            durationMinutes = row.durationMinutes.toInt(),
            groupId = row.groupId,
            date = row.date,
            startMinute = row.startMinute.toInt(),
            isCompleted = row.isCompleted == 1L,
            reminderMinutes = row.reminderMinutes.toInt(),
            groupName = row.group_name,
            groupColorHex = row.group_color_hex,
        )
    }

    suspend fun insertTask(task: DayTask) {
        q.insertTask(
            task.name, task.description, task.durationMinutes.toLong(),
            task.groupId, task.date, task.startMinute.toLong(),
            if (task.isCompleted) 1L else 0L,
            task.reminderMinutes.toLong()
        )
    }

    suspend fun updateTask(task: DayTask) {
        q.updateTask(
            task.name, task.description, task.durationMinutes.toLong(),
            task.groupId, task.date, task.startMinute.toLong(),
            if (task.isCompleted) 1L else 0L,
            task.reminderMinutes.toLong(),
            task.id
        )
    }

    suspend fun deleteTask(id: Long) {
        q.deleteTask(id)
    }

    suspend fun markTaskComplete(id: Long) {
        q.markTaskComplete(id)
    }

    // Schedule helper
    fun observeSchedule(date: String): Flow<DaySchedule> {
        return observeTasksForDate(date).map { tasks ->
            DaySchedule(date = date, tasks = tasks)
        }
    }
}
