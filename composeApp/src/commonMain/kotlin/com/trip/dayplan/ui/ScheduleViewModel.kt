package com.trip.dayplan.ui

import com.trip.dayplan.data.DayPlanRepository
import com.trip.dayplan.domain.DayTask
import com.trip.dayplan.domain.TaskGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * UI state for the day schedule screen.
 */
data class ScheduleUiState(
    val date: String = today(),
    val tasks: List<DayTask> = emptyList(),
    val groups: List<TaskGroup> = emptyList(),
    val isLoading: Boolean = true,
    val showAddTaskDialog: Boolean = false,
    val showAddGroupDialog: Boolean = false,
    val editingTask: DayTask? = null,
    val prefillStartMinute: Int? = null,   // when tapping timeline, pre-fill this time
) {
    companion object {
        fun today(): String = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toString()
    }
}

/**
 * ViewModel for the schedule screen. Manages tasks, groups, and UI state.
 */
class ScheduleViewModel(
    private val repository: DayPlanRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        // Combine tasks and groups flows
        combine(
            repository.observeGroups(),
            repository.observeTasksForDate(_state.value.date),
        ) { groups, tasks ->
            _state.update {
                it.copy(
                    tasks = tasks,
                    groups = groups,
                    isLoading = false,
                )
            }
        }.launchIn(scope)
    }

    fun prevDay() {
        val currentDate = LocalDate.parse(_state.value.date)
        val newDate = currentDate.minus(DatePeriod(days = 1))
        changeDate(newDate.toString())
    }

    fun nextDay() {
        val currentDate = LocalDate.parse(_state.value.date)
        val newDate = currentDate.plus(DatePeriod(days = 1))
        changeDate(newDate.toString())
    }

    fun changeDate(newDate: String) {
        scope.launch {
            val tasks = repository.getTasksForDate(newDate)
            _state.update { it.copy(date = newDate, tasks = tasks) }
        }
    }

    fun showAddTaskDialog(prefillMinute: Int? = null) {
        _state.update { it.copy(showAddTaskDialog = true, editingTask = null, prefillStartMinute = prefillMinute) }
    }

    fun showEditTask(task: DayTask) {
        _state.update { it.copy(showAddTaskDialog = true, editingTask = task, prefillStartMinute = null) }
    }

    fun dismissAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = false, editingTask = null, prefillStartMinute = null) }
    }

    fun showAddGroupDialog() {
        _state.update { it.copy(showAddGroupDialog = true) }
    }

    fun dismissAddGroupDialog() {
        _state.update { it.copy(showAddGroupDialog = false) }
    }

    suspend fun saveTask(task: DayTask) {
        if (task.id == 0L) {
            repository.insertTask(task)
        } else {
            repository.updateTask(task)
        }
    }

    suspend fun deleteTask(id: Long) {
        repository.deleteTask(id)
    }

    suspend fun toggleTaskComplete(task: DayTask) {
        repository.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun moveTask(task: DayTask, newStartMinute: Int) {
        repository.updateTask(task.copy(startMinute = newStartMinute))
    }

    suspend fun saveGroup(group: TaskGroup) {
        if (group.id == 0L) {
            repository.insertGroup(group.name, group.colorHex, group.sortOrder)
        } else {
            repository.updateGroup(group)
        }
    }

    suspend fun deleteGroup(id: Long) {
        repository.deleteGroup(id)
    }

    /**
     * Get the next upcoming task from now.
     */
    fun getNextTask(): DayTask? {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentMinute = now.hour * 60 + now.minute
        return _state.value.tasks
            .filter { !it.isCompleted && it.startMinute >= currentMinute }
            .sortedBy { it.startMinute }
            .firstOrNull()
    }

    /**
     * Get tasks that are currently active (in progress right now).
     */
    fun getActiveTasks(): List<DayTask> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentMinute = now.hour * 60 + now.minute
        return _state.value.tasks.filter { task ->
            !task.isCompleted && currentMinute in task.startMinute until (task.startMinute + task.durationMinutes)
        }
    }
}
