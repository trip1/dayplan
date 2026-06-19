package com.trip.dayplan.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeTaskBoardTest {
    @Test
    fun splitsTasksIntoEverydayHomeLists() {
        val tasks = listOf(
            DayTask(id = 1, name = "Pack lunches", startMinute = 7 * 60, durationMinutes = 30),
            DayTask(id = 2, name = "Fold towels", startMinute = 9 * 60, durationMinutes = 30),
            DayTask(id = 3, name = "Start dinner", startMinute = 17 * 60, durationMinutes = 45),
            DayTask(id = 4, name = "Pay water bill", isCompleted = true),
        )

        val lists = HomeTaskBoard.split(tasks, nowMinute = 9 * 60 + 10)

        assertEquals(listOf("Fold towels"), lists.doingNow.map { it.name })
        assertEquals(listOf("Pack lunches", "Start dinner"), lists.toDo.map { it.name })
        assertEquals(listOf("Pay water bill"), lists.done.map { it.name })
    }

    @Test
    fun usesHomeFriendlyDefaultAreas() {
        assertEquals(
            listOf("Home", "Family", "Errands", "Self-care"),
            DefaultGroups.groups.map { it.name },
        )
    }
}
