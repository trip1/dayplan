package com.trip.dayplan.domain

/**
 * Small shared helper for presenting the day like a simple home task board.
 *
 * We intentionally use everyday words in the UI:
 * - To do: not finished and not happening right now
 * - Doing now: happening at the current time
 * - Done: finished
 */
data class HomeTaskLists(
    val toDo: List<DayTask>,
    val doingNow: List<DayTask>,
    val done: List<DayTask>,
)

object HomeTaskBoard {
    fun split(tasks: List<DayTask>, nowMinute: Int): HomeTaskLists {
        val sortedTasks = tasks.sortedWith(compareBy<DayTask> { it.isCompleted }.thenBy { it.startMinute })
        val done = sortedTasks.filter { it.isCompleted }
        val doingNow = sortedTasks.filter { task ->
            !task.isCompleted && nowMinute in task.startMinute until (task.startMinute + task.durationMinutes)
        }
        val toDo = sortedTasks.filter { task ->
            !task.isCompleted && task !in doingNow
        }

        return HomeTaskLists(
            toDo = toDo,
            doingNow = doingNow,
            done = done,
        )
    }
}
