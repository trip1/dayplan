package com.trip.dayplan.data

import com.trip.dayplan.domain.DaySchedule
import com.trip.dayplan.domain.DayTask
import com.trip.dayplan.domain.TaskGroup
import kotlinx.coroutines.flow.Flow

/**
 * Common schedule persistence API.
 *
 * SQLDelight-backed platforms use [DayPlanRepository]. Browser builds use a
 * localStorage-backed implementation so the Compose web app can ship as a
 * static bundle without a database worker.
 */
interface DayPlanStore {
    fun observeGroups(): Flow<List<TaskGroup>>
    suspend fun getGroups(): List<TaskGroup>
    suspend fun insertGroup(name: String, colorHex: String, sortOrder: Int)
    suspend fun updateGroup(group: TaskGroup)
    suspend fun deleteGroup(id: Long)

    fun observeTasksForDate(date: String): Flow<List<DayTask>>
    suspend fun getTasksForDate(date: String): List<DayTask>
    suspend fun getTaskById(id: Long): DayTask?
    suspend fun insertTask(task: DayTask)
    suspend fun updateTask(task: DayTask)
    suspend fun deleteTask(id: Long)
    suspend fun markTaskComplete(id: Long)

    fun observeSchedule(date: String): Flow<DaySchedule>
}
