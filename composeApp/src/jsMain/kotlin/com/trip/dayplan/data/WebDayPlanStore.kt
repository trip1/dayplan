package com.trip.dayplan.data

import com.trip.dayplan.domain.DaySchedule
import com.trip.dayplan.domain.DayTask
import com.trip.dayplan.domain.DefaultGroups
import com.trip.dayplan.domain.TaskGroup
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Browser persistence for the static Web/Wasm build.
 *
 * The native apps keep using SQLDelight. The web build stores tasks and groups
 * in localStorage so it can be distributed as a simple static site.
 */
class WebDayPlanStore : DayPlanStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val state = MutableStateFlow(readState())

    init {
        if (state.value.groups.isEmpty()) {
            val seeded = state.value.copy(
                groups = DefaultGroups.groups.mapIndexed { index, group ->
                    group.copy(id = (index + 1).toLong())
                },
                nextGroupId = DefaultGroups.groups.size.toLong() + 1,
            )
            state.value = seeded
            writeState(seeded)
        }
    }

    override fun observeGroups(): Flow<List<TaskGroup>> = state.map { it.groups.sortedBy(TaskGroup::sortOrder) }

    override suspend fun getGroups(): List<TaskGroup> = observeGroups().first()

    override suspend fun insertGroup(name: String, colorHex: String, sortOrder: Int) {
        updateState { current ->
            val group = TaskGroup(
                id = current.nextGroupId,
                name = name,
                colorHex = colorHex,
                sortOrder = sortOrder,
            )
            current.copy(
                groups = current.groups + group,
                nextGroupId = current.nextGroupId + 1,
            )
        }
    }

    override suspend fun updateGroup(group: TaskGroup) {
        updateState { current ->
            current.copy(groups = current.groups.map { if (it.id == group.id) group else it })
        }
    }

    override suspend fun deleteGroup(id: Long) {
        updateState { current ->
            current.copy(
                groups = current.groups.filterNot { it.id == id },
                tasks = current.tasks.map { task ->
                    if (task.groupId == id) task.copy(groupId = null) else task
                },
            )
        }
    }

    override fun observeTasksForDate(date: String): Flow<List<DayTask>> = state.map { current ->
        current.tasks
            .filter { it.date == date }
            .sortedBy { it.startMinute }
            .map { it.withGroup(current.groups) }
    }

    override suspend fun getTasksForDate(date: String): List<DayTask> = observeTasksForDate(date).first()

    override suspend fun getTaskById(id: Long): DayTask? {
        val current = state.value
        return current.tasks.firstOrNull { it.id == id }?.withGroup(current.groups)
    }

    override suspend fun insertTask(task: DayTask) {
        updateState { current ->
            val saved = task.copy(id = current.nextTaskId).withoutGroupSnapshot()
            current.copy(
                tasks = current.tasks + saved,
                nextTaskId = current.nextTaskId + 1,
            )
        }
    }

    override suspend fun updateTask(task: DayTask) {
        updateState { current ->
            current.copy(
                tasks = current.tasks.map { if (it.id == task.id) task.withoutGroupSnapshot() else it },
            )
        }
    }

    override suspend fun deleteTask(id: Long) {
        updateState { current -> current.copy(tasks = current.tasks.filterNot { it.id == id }) }
    }

    override suspend fun markTaskComplete(id: Long) {
        updateState { current ->
            current.copy(tasks = current.tasks.map { task ->
                if (task.id == id) task.copy(isCompleted = true) else task
            })
        }
    }

    override fun observeSchedule(date: String): Flow<DaySchedule> {
        return combine(observeGroups(), observeTasksForDate(date)) { groups, tasks ->
            DaySchedule(date = date, tasks = tasks, groups = groups)
        }
    }

    private fun updateState(update: (WebDayPlanState) -> WebDayPlanState) {
        val updated = update(state.value)
        state.value = updated
        writeState(updated)
    }

    private fun readState(): WebDayPlanState {
        val raw = window.localStorage.getItem(KEY) ?: return WebDayPlanState()
        return runCatching { json.decodeFromString<WebDayPlanState>(raw) }.getOrDefault(WebDayPlanState())
    }

    private fun writeState(value: WebDayPlanState) {
        window.localStorage.setItem(KEY, json.encodeToString(value))
    }

    private fun DayTask.withGroup(groups: List<TaskGroup>): DayTask {
        val group = groupId?.let { id -> groups.firstOrNull { it.id == id } }
        return copy(groupName = group?.name, groupColorHex = group?.colorHex)
    }

    private fun DayTask.withoutGroupSnapshot(): DayTask = copy(groupName = null, groupColorHex = null)

    private companion object {
        const val KEY = "dayplan.data"
    }
}

@Serializable
private data class WebDayPlanState(
    val groups: List<TaskGroup> = emptyList(),
    val tasks: List<DayTask> = emptyList(),
    val nextGroupId: Long = 1,
    val nextTaskId: Long = 1,
)
