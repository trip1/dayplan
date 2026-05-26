package com.trip.dayplan.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trip.dayplan.domain.AppSettings
import com.trip.dayplan.domain.DayTask
import com.trip.dayplan.domain.TaskGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

/**
 * Parse a hex color string (#RRGGBB or #RGB) into a Compose Color.
 */
private fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return when (cleaned.length) {
        3 -> {
            val r = cleaned[0].toString().repeat(2).toInt(16)
            val g = cleaned[1].toString().repeat(2).toInt(16)
            val b = cleaned[2].toString().repeat(2).toInt(16)
            Color(r, g, b)
        }
        6 -> {
            val r = cleaned.substring(0, 2).toInt(16)
            val g = cleaned.substring(2, 4).toInt(16)
            val b = cleaned.substring(4, 6).toInt(16)
            Color(r, g, b)
        }
        8 -> {
            val a = cleaned.substring(0, 2).toInt(16)
            val r = cleaned.substring(2, 4).toInt(16)
            val g = cleaned.substring(4, 6).toInt(16)
            val b = cleaned.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> Color.Gray
    }
}

/**
 * Convert a date string (YYYY-MM-DD) to epoch milliseconds at midnight UTC.
 */
private fun todayMillis(dateStr: String): Long {
    val parts = dateStr.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    return kotlinx.datetime.LocalDateTime(year, month, day, 0, 0)
        .toInstant(kotlinx.datetime.TimeZone.UTC)
        .toEpochMilliseconds()
}

// Constants
private const val START_HOUR = 6
private const val END_HOUR = 23
private const val TOTAL_MINUTES = (END_HOUR - START_HOUR) * 60
private const val PIXELS_PER_MINUTE = 1.5f

/**
 * Get tasks that will end within 5 minutes from now.
 */
private fun getExpiringTasks(tasks: List<DayTask>, nowMinute: Int): List<DayTask> {
    return tasks.filter { task ->
        if (task.isCompleted) return@filter false
        val endMinute = task.startMinute + task.durationMinutes
        val timeLeft = endMinute - nowMinute
        timeLeft in 1..5
    }
}

@Composable
private fun NotificationTicker(viewModel: ScheduleViewModel) {
    val state by viewModel.state.collectAsState()
    val nowMinute by produceState(initialValue = 0) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            value = now.hour * 60 + now.minute
            delay(30_000) // tick every 30s
        }
    }

    val nextTask = viewModel.getNextTask()
    val endingTasks = getExpiringTasks(viewModel.getFilteredTasks(), nowMinute)

    if (nextTask == null && endingTasks.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = DayPlanTheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Next task
            if (nextTask != null) {
                val timeUntil = nextTask.startMinute - nowMinute
                val timeText = if (timeUntil < 60) "in ${timeUntil}m" else "in ${timeUntil / 60}h ${timeUntil % 60}m"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(parseHexColor(nextTask.colorHex), RoundedCornerShape(2.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next: ${nextTask.name} · $timeText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DayPlanTheme.textPrimary,
                    )
                }
            }
            // Ending soon
            endingTasks.forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(parseHexColor(task.colorHex), RoundedCornerShape(2.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ending: ${task.name} · ${task.durationMinutes - (nowMinute - task.startMinute)}m left",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53E3E),
                    )
                }
            }
        }
    }
}

@Composable
fun App(viewModel: ScheduleViewModel) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // Schedule notifications for today's tasks
    LaunchedEffect(state.tasks) {
        NotificationManager.cancelAllNotifications()
        state.tasks.forEach { task ->
            if (!task.isCompleted && task.reminderMinutes > 0) {
                // Use per-task reminder time
                val endMinute = task.startMinute + task.durationMinutes
                val reminderTime = endMinute - task.reminderMinutes
                if (reminderTime >= 0) {
                    NotificationManager.scheduleTaskReminder(
                        task.name,
                        minutesBefore = task.reminderMinutes,
                        timestampMillis = todayMillis(task.date) + reminderTime * 60_000L,
                    )
                }
            }
        }
        // Next task notification
        viewModel.getNextTask()?.let { next ->
            NotificationManager.scheduleNextTaskNotification(
                next.name,
                timestampMillis = todayMillis(next.date) + next.startMinute * 60_000L,
            )
        }
    }

    DayPlanAppTheme(darkTheme = state.settings.darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DayPlanTheme.background,
        ) {
            Column {
                // Notification ticker
                NotificationTicker(viewModel)

                // Group filter chips
                GroupFilterBar(
                    groups = state.groups,
                    activeGroupId = state.activeFilterGroupId,
                    onToggleFilter = { viewModel.toggleGroupFilter(it) },
                )

                // Top bar
                DateHeader(
                    date = state.date,
                    onPrevDay = { viewModel.prevDay() },
                    onNextDay = { viewModel.nextDay() },
                    onSettingsClick = { viewModel.showSettingsDialog() },
                )

                // Timeline
                TimelineView(
                    tasks = viewModel.getFilteredTasks(),
                    groups = state.groups,
                    onAddTask = { viewModel.showAddTaskDialog() },
                    onTaskClick = { viewModel.showEditTask(it) },
                    onTaskLongPress = { task ->
                        scope.launch {
                            viewModel.toggleTaskComplete(task)
                        }
                    },
                    onTaskDrag = { task, newMinute ->
                        scope.launch {
                            val clamped = newMinute.coerceIn(state.settings.startHour * 60, state.settings.endHour * 60 - 5)
                            viewModel.moveTask(task, clamped)
                        }
                    },
                    onEmptyTap = { minute ->
                        val rounded = ((minute + 2) / 5) * 5
                        viewModel.showAddTaskDialog(prefillMinute = rounded)
                    },
                    onSwipeDelete = { task ->
                        scope.launch {
                            viewModel.deleteTask(task.id)
                        }
                    },
                )
            }

            // Dialogs
            if (state.showAddTaskDialog) {
                AddTaskDialog(
                    editingTask = state.editingTask,
                    groups = state.groups,
                    prefillStartMinute = state.prefillStartMinute,
                    defaultReminder = state.settings.defaultReminder,
                    onDismiss = { viewModel.dismissAddTaskDialog() },
                    onSave = { task ->
                        scope.launch {
                            viewModel.saveTask(task)
                            viewModel.dismissAddTaskDialog()
                        }
                    },
                    onDelete = { id ->
                        scope.launch {
                            viewModel.deleteTask(id)
                            viewModel.dismissAddTaskDialog()
                        }
                    },
                )
            }

            if (state.showAddGroupDialog) {
                AddGroupDialog(
                    onDismiss = { viewModel.dismissAddGroupDialog() },
                    onSave = { group ->
                        scope.launch {
                            viewModel.saveGroup(group)
                            viewModel.dismissAddGroupDialog()
                        }
                    },
                )
            }

            if (state.showSettingsDialog) {
                SettingsDialog(
                    settings = state.settings,
                    onDismiss = { viewModel.dismissSettingsDialog() },
                    onSave = { updated ->
                        scope.launch {
                            viewModel.updateSetting { updated }
                            viewModel.dismissSettingsDialog()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GroupFilterBar(
    groups: List<TaskGroup>,
    activeGroupId: Long?,
    onToggleFilter: (Long?) -> Unit,
) {
    if (groups.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // "All" chip
        FilterChip(
            selected = activeGroupId == null,
            onClick = { onToggleFilter(null) },
            label = { Text("All", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DayPlanTheme.primary,
                selectedLabelColor = Color.White,
            ),
        )
        groups.forEach { group ->
            val groupColor = try { parseHexColor(group.colorHex) } catch (e: Exception) { DayPlanTheme.primary }
            FilterChip(
                selected = activeGroupId == group.id,
                onClick = { onToggleFilter(group.id) },
                label = { Text(group.name, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = groupColor,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun DateHeader(
    date: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onSettingsClick: () -> Unit = {},
) {
    val parsed = kotlin.runCatching {
        kotlinx.datetime.LocalDate.parse(date)
    }.getOrNull()

    val formatted = parsed?.let {
        val dayName = when (it.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Monday"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Tuesday"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wednesday"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Thursday"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Friday"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Saturday"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }
        val monthName = when (it.monthNumber) {
            1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
            5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
            9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
            else -> ""
        }
        "$dayName, $monthName ${it.dayOfMonth}"
    } ?: date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevDay) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous day", tint = DayPlanTheme.textSecondary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = DayPlanTheme.textPrimary,
            )
            Text(
                text = "Tap a task to edit · Long press to complete · Swipe left to delete",
                fontSize = 11.sp,
                color = DayPlanTheme.textSecondary,
            )
        }
        Row {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Settings,
                    "Settings",
                    tint = DayPlanTheme.textSecondary,
                )
            }
            IconButton(onClick = onNextDay) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next day", tint = DayPlanTheme.textSecondary)
            }
        }
    }
    HorizontalDivider(color = DayPlanTheme.divider)
}

@Composable
private fun TimelineView(
    tasks: List<DayTask>,
    groups: List<TaskGroup>,
    onAddTask: () -> Unit,
    onTaskClick: (DayTask) -> Unit,
    onTaskLongPress: (DayTask) -> Unit,
    onTaskDrag: (DayTask, Int) -> Unit,
    onEmptyTap: (Int) -> Unit = {},
    onSwipeDelete: (DayTask) -> Unit = {},
) {
    val density = LocalDensity.current
    val pixelsPerMinute = with(density) { PIXELS_PER_MINUTE.dp.toPx() }

    // Current time ticker
    val currentMinute by produceState(initialValue = 0f) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            value = (now.hour * 60f + now.minute)
            delay(60_000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val minute = (offset.y / pixelsPerMinute).toInt() + START_HOUR * 60
                    if (minute in START_HOUR * 60 until END_HOUR * 60) {
                        onEmptyTap(minute)
                    }
                }
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Time labels + track
            Row(modifier = Modifier.fillMaxWidth()) {
                // Time labels column
                Column(modifier = Modifier.width(56.dp)) {
                    for (hour in START_HOUR..END_HOUR) {
                        Box(
                            modifier = Modifier
                                .height((60 * PIXELS_PER_MINUTE).dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd,
                        ) {
                            Text(
                                text = formatHour(hour),
                                fontSize = 11.sp,
                                color = DayPlanTheme.textSecondary,
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp),
                            )
                        }
                    }
                }

                // Timeline track
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height((TOTAL_MINUTES * PIXELS_PER_MINUTE).dp)
                        .background(DayPlanTheme.background),
                ) {
                    // Hour lines
                    for (hour in START_HOUR..END_HOUR) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .offset(y = ((hour - START_HOUR) * 60 * PIXELS_PER_MINUTE).dp),
                        ) {
                            drawLine(
                                color = DayPlanTheme.divider,
                                start = Offset(0f, 0.5f),
                                end = Offset(size.width, 0.5f),
                                strokeWidth = 1f,
                            )
                        }
                    }

                    // Task blocks
                    tasks.forEach { task ->
                        TaskBlock(
                            task = task,
                            pixelsPerMinute = pixelsPerMinute,
                            onClick = { onTaskClick(task) },
                            onLongPress = { onTaskLongPress(task) },
                            onDrag = { dy ->
                                val deltaMinutes = (dy / pixelsPerMinute).toInt()
                                val newMinute = task.startMinute + deltaMinutes
                                if (newMinute in START_HOUR * 60 until END_HOUR * 60) {
                                    onTaskDrag(task, newMinute)
                                }
                            },
                            onSwipeDelete = { onSwipeDelete(task) },
                        )
                    }

                    // Now indicator
                    val nowY = ((currentMinute - START_HOUR * 60) * PIXELS_PER_MINUTE).dp
                    if (currentMinute >= START_HOUR * 60 && currentMinute <= END_HOUR * 60) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = nowY),
                        ) {
                            drawLine(
                                color = DayPlanTheme.nowIndicator,
                                start = Offset(0f, 1f),
                                end = Offset(size.width, 1f),
                                strokeWidth = 2f,
                            )
                        }
                        // Now dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .offset(y = nowY - 5.dp)
                                .align(Alignment.CenterStart),
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = DayPlanTheme.nowIndicator,
                                    radius = size.minDimension / 2,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // FAB
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        FloatingActionButton(
            onClick = onAddTask,
            containerColor = DayPlanTheme.primary,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, "Add task")
        }
    }
}

@Composable
private fun TaskBlock(
    task: DayTask,
    pixelsPerMinute: Float,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDrag: (Float) -> Unit,
    onSwipeDelete: () -> Unit = {},
) {
    val blockTop = ((task.startMinute - START_HOUR * 60) * pixelsPerMinute).dp
    val blockHeight = (task.durationMinutes * pixelsPerMinute).dp.coerceAtLeast(28.dp)
    val taskColor = try { parseHexColor(task.colorHex) } catch (e: Exception) { DayPlanTheme.primary }

    val isCompleted = task.isCompleted

    // Swipe state
    var swipeOffsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 60f // px to trigger delete

    Box(
        modifier = Modifier
            .offset(y = blockTop)
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .fillMaxWidth()
            .height(blockHeight)
            .background(
                color = if (isCompleted) taskColor.copy(alpha = 0.3f) else taskColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (swipeOffsetX < -swipeThreshold) {
                            onSwipeDelete()
                        }
                        swipeOffsetX = 0f
                    },
                ) { change, dragAmount ->
                    change.consume()
                    if (abs(dragAmount.x) > abs(dragAmount.y)) {
                        swipeOffsetX += dragAmount.x
                        // Clamp to left only
                        swipeOffsetX = swipeOffsetX.coerceAtMost(0f)
                    } else {
                        onDrag(dragAmount.y)
                    }
                }
            }
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() },
                )
            },
    ) {
        // Delete indicator (red background revealed on swipe)
        if (swipeOffsetX < 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFE53E3E), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Delete,
                    "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 12.dp).size(18.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .offset(x = swipeOffsetX.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    "Completed",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (blockHeight > 36.dp) {
                    Text(
                        text = "${formatTime(task.startTime)} – ${formatTime(task.endTime)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    editingTask: DayTask?,
    groups: List<TaskGroup>,
    prefillStartMinute: Int?,
    defaultReminder: Int = 5,
    onDismiss: () -> Unit,
    onSave: (DayTask) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var name by remember { mutableStateOf(editingTask?.name ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var duration by remember { mutableStateOf((editingTask?.durationMinutes ?: 30).toString()) }
    val defaultMinute = editingTask?.startMinute ?: prefillStartMinute ?: 9 * 60
    var startHour by remember { mutableStateOf(defaultMinute / 60) }
    var startMin by remember { mutableStateOf(defaultMinute % 60) }
    var selectedGroupId by remember { mutableStateOf<Long?>(editingTask?.groupId) }
    var reminderMinutes by remember { mutableStateOf(editingTask?.reminderMinutes ?: defaultReminder) }

    val date = ScheduleUiState.today()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingTask == null) "New Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Start time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = startHour.toString().padStart(2, '0'),
                            onValueChange = { v -> startHour = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(0, 23) ?: 0 },
                            label = { Text("Hour") },
                            modifier = Modifier.width(72.dp),
                            singleLine = true,
                        )
                        Text(":", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        OutlinedTextField(
                            value = startMin.toString().padStart(2, '0'),
                            onValueChange = { v -> startMin = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                            label = { Text("Min") },
                            modifier = Modifier.width(72.dp),
                            singleLine = true,
                        )
                    }
                }
                // Reminder picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Remind", fontSize = 12.sp, color = DayPlanTheme.textSecondary, modifier = Modifier.width(56.dp))
                    val reminderOptions = listOf(0, 1, 3, 5, 10, 15, 30)
                    reminderOptions.forEach { mins ->
                        FilterChip(
                            selected = reminderMinutes == mins,
                            onClick = { reminderMinutes = mins },
                            label = { Text(if (mins == 0) "Off" else "${mins}m", fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                }
                // Group selector
                Text("Group", fontSize = 12.sp, color = DayPlanTheme.textSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // None option
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = { selectedGroupId = null },
                        label = { Text("None") },
                        leadingIcon = if (selectedGroupId == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                    )
                    groups.forEach { group ->
                        val groupColor = try { parseHexColor(group.colorHex) } catch (e: Exception) { DayPlanTheme.primary }
                        FilterChip(
                            selected = selectedGroupId == group.id,
                            onClick = { selectedGroupId = group.id },
                            label = { Text(group.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = groupColor.copy(alpha = 0.2f),
                            ),
                            leadingIcon = if (selectedGroupId == group.id) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else {
                                {
                                    Box(
                                        Modifier
                                            .size(12.dp)
                                            .background(groupColor, RoundedCornerShape(3.dp)),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationMin = duration.toIntOrNull()?.coerceIn(5, 480) ?: 30
                    val roundedMin = (startMin / 5) * 5 // Round to nearest 5 min
                    val task = DayTask(
                        id = editingTask?.id ?: 0,
                        name = name.ifBlank { "Untitled" },
                        description = description,
                        durationMinutes = durationMin,
                        groupId = selectedGroupId,
                        date = date,
                        startMinute = startHour * 60 + roundedMin,
                        reminderMinutes = reminderMinutes,
                    )
                    onSave(task)
                },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (editingTask != null) {
                    TextButton(onClick = { onDelete(editingTask.id) }) {
                        Text("Delete", color = Color.Red)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun AddGroupDialog(
    onDismiss: () -> Unit,
    onSave: (TaskGroup) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4A6741") }

    val presetColors = listOf(
        "#4A6741", // Sage green
        "#E8913A", // Warm orange
        "#5B8DB8", // Steel blue
        "#8B5CF6", // Purple
        "#EF4444", // Red
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#6366F1", // Indigo
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Color", fontSize = 12.sp, color = DayPlanTheme.textSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presetColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    parseHexColor(color),
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                    } else {
                                        Modifier
                                    }
                                ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(TaskGroup(name = name, colorHex = selectedColor))
                    }
                },
                enabled = name.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// Utility functions
private fun formatHour(hour: Int): String {
    return if (hour == 0 || hour == 24) {
        "12 AM"
    } else if (hour < 12) {
        "$hour AM"
    } else if (hour == 12) {
        "12 PM"
    } else {
        "${hour - 12} PM"
    }
}

private fun formatTime(time: kotlinx.datetime.LocalTime): String {
    val hour = time.hour
    val min = time.minute.toString().padStart(2, '0')
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0 || hour == 12) 12 else if (hour > 12) hour - 12 else hour
    return "$displayHour:$min $suffix"
}

@Composable
private fun SettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (AppSettings) -> Unit,
) {
    var darkTheme by remember { mutableStateOf(settings.darkTheme) }
    var startHour by remember { mutableStateOf(settings.startHour.toString()) }
    var endHour by remember { mutableStateOf(settings.endHour.toString()) }
    var defaultDuration by remember { mutableStateOf(settings.defaultDuration.toString()) }
    var defaultReminder by remember { mutableStateOf(settings.defaultReminder.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Dark Mode", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { darkTheme = it },
                    )
                }

                HorizontalDivider(color = DayPlanTheme.divider)

                // Timeline hours
                Text("Timeline Hours", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Start:", modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(64.dp),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("End:", modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(64.dp),
                        singleLine = true,
                    )
                }

                // Default task duration
                Text("Default Task Duration", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = defaultDuration,
                        onValueChange = { defaultDuration = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes") },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                    )
                }

                // Default reminder
                Text("Default Reminder", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = defaultReminder,
                        onValueChange = { defaultReminder = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes before end") },
                        modifier = Modifier.width(140.dp),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        settings.copy(
                            darkTheme = darkTheme,
                            startHour = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 6,
                            endHour = endHour.toIntOrNull()?.coerceIn(1, 24) ?: 23,
                            defaultDuration = defaultDuration.toIntOrNull()?.coerceIn(5, 480) ?: 30,
                            defaultReminder = defaultReminder.toIntOrNull()?.coerceIn(0, 60) ?: 5,
                        )
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
