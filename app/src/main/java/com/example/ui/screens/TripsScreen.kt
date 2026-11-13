package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TravelPlan
import com.example.ui.viewmodel.TravelViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    viewModel: TravelViewModel,
    modifier: Modifier = Modifier
) {
    val travelPlans by viewModel.allTravelPlans.collectAsState()
    val selectedId by viewModel.selectedTravelId.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var planToEdit by remember { mutableStateOf<TravelPlan?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "旅行プランナー",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "家族の旅を一元管理・計画",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        val isDark by viewModel.isDarkMode.collectAsState()
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "テーマ切替"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("旅行を計画") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_trip_fab")
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (travelPlans.isEmpty()) {
                EmptyTripsState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(travelPlans) { plan ->
                        TravelPlanCard(
                            plan = plan,
                            isSelected = plan.id == selectedId,
                            onSelect = { viewModel.selectTravel(plan.id) },
                            onEdit = { planToEdit = plan },
                            onDelete = { viewModel.deleteTravelPlan(plan) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Fab padding
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TripFormDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, dest, year, days, transport, accommodation, status, memo, date ->
                viewModel.addTravelPlan(title, dest, year, days, transport, accommodation, status, memo, date)
                showAddDialog = false
            }
        )
    }

    if (planToEdit != null) {
        TripFormDialog(
            existingPlan = planToEdit,
            onDismiss = { planToEdit = null },
            onSave = { title, dest, year, days, transport, accommodation, status, memo, date ->
                planToEdit?.let {
                    viewModel.updateTravelPlan(
                        it.copy(
                            title = title,
                            destinations = dest,
                            year = year,
                            days = days,
                            transport = transport,
                            accommodation = accommodation,
                            reservationStatus = status,
                            memo = memo,
                            startDateString = date
                        )
                    )
                }
                planToEdit = null
            }
        )
    }
}

@Composable
fun EmptyTripsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Landscape,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "旅行の計画がありません",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "家族との新しい思い出作りに、最初の旅行計画を追加しましょう！",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("最初の旅行を作成")
        }
    }
}

@Composable
fun TravelPlanCard(
    plan: TravelPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val countdown = remember(plan.startDateString) { getCountdownDays(plan.startDateString) }
    var showMenu by remember { mutableStateOf(false) }

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
            ),
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable(onClick = onSelect)
            .testTag("travel_plan_card_${plan.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year & Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.year,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ReservationBadge(status = plan.reservationStatus)
                }

                // Options Button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("削除", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title & Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Countdown Badge
                if (countdown != "日付未定" && countdown != "終了") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = countdown,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else if (countdown == "本日！") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = countdown,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Destinations
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = plan.destinations,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days & Transport & Accommodation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Days
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plan.days,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Transport
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val transportIcon = when (plan.transport) {
                        "電車" -> Icons.Default.Train
                        "車" -> Icons.Default.DirectionsCar
                        "飛行機" -> Icons.Default.Flight
                        else -> Icons.Default.QuestionMark
                    }
                    Icon(
                        imageVector = transportIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plan.transport,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Accommodation
                if (plan.accommodation.isNotBlank() && plan.accommodation != "未定") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = plan.accommodation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (plan.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = plan.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "選択中 (タブで詳細を編集できます)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationBadge(status: String) {
    val containerColor = when (status) {
        "予約済み" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        "未予約" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }

    val contentColor = when (status) {
        "予約済み" -> MaterialTheme.colorScheme.secondary
        "未予約" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormDialog(
    existingPlan: TravelPlan? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        destinations: String,
        year: String,
        days: String,
        transport: String,
        accommodation: String,
        reservationStatus: String,
        memo: String,
        startDateString: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(existingPlan?.title ?: "") }
    var destinations by remember { mutableStateOf(existingPlan?.destinations ?: "") }
    var year by remember { mutableStateOf(existingPlan?.year ?: "") }
    var days by remember { mutableStateOf(existingPlan?.days ?: "") }
    var transport by remember { mutableStateOf(existingPlan?.transport ?: "電車") }
    var accommodation by remember { mutableStateOf(existingPlan?.accommodation ?: "") }
    var reservationStatus by remember { mutableStateOf(existingPlan?.reservationStatus ?: "未予約") }
    var memo by remember { mutableStateOf(existingPlan?.memo ?: "") }
    var startDateString by remember { mutableStateOf(existingPlan?.startDateString ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingPlan == null) "旅行プランの追加" else "旅行プランの編集",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("旅行タイトル *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_field")
                    )
                }
                item {
                    OutlinedTextField(
                        value = destinations,
                        onValueChange = { destinations = it },
                        label = { Text("行き先 *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("年 (例: 2026年, 2027年（予定）)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = startDateString,
                        onValueChange = { startDateString = it },
                        label = { Text("出発日 (YYYY-MM-DD / カウントダウン用)") },
                        placeholder = { Text("2026-10-15") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it },
                        label = { Text("日数 (例: 1泊2日, 未定)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("移動手段", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("電車", "車", "飛行機", "未定").forEach { option ->
                            FilterChip(
                                selected = transport == option,
                                onClick = { transport = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = accommodation,
                        onValueChange = { accommodation = it },
                        label = { Text("宿泊先 (例: ホテル名, 未定)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("予約状況", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("予約済み", "未予約", "未定").forEach { option ->
                            FilterChip(
                                selected = reservationStatus == option,
                                onClick = { reservationStatus = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        label = { Text("メモ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && destinations.isNotBlank()) {
                        onSave(
                            title,
                            destinations,
                            if (year.isBlank()) "未定" else year,
                            if (days.isBlank()) "未定" else days,
                            transport,
                            if (accommodation.isBlank()) "未定" else accommodation,
                            reservationStatus,
                            memo,
                            startDateString
                        )
                    }
                },
                enabled = title.isNotBlank() && destinations.isNotBlank(),
                modifier = Modifier.testTag("form_save_button")
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

fun getCountdownDays(startDateStr: String): String {
    if (startDateStr.isBlank()) return "日付未定"
    return try {
        // Set context reference date (metadata time is 2026-07-07)
        val today = LocalDate.of(2026, 7, 7)
        val startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        val days = ChronoUnit.DAYS.between(today, startDate)
        when {
            days < 0 -> "終了"
            days == 0L -> "本日！"
            else -> "あと ${days}日"
        }
    } catch (e: Exception) {
        "日付未定"
    }
}
