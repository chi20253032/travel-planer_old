package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Member
import com.example.data.models.OnsenPlan
import com.example.ui.viewmodel.TravelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnsenScreen(
    viewModel: TravelViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPlan by viewModel.selectedTravelPlan.collectAsState()
    val members by viewModel.allMembers.collectAsState()
    val onsenPlans by viewModel.selectedOnsenPlans.collectAsState()

    var editingMember by remember { mutableStateOf<Member?>(null) }
    var editingOnsenPlan by remember { mutableStateOf<OnsenPlan?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "温泉計画 & メンバー",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "家族の温浴スケジュール・情報管理",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Members Management Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "同行メンバー情報 (編集可能)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showAddMemberDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("メンバー追加", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            items(members) { member ->
                MemberRowCard(
                    member = member,
                    onEditClick = { editingMember = member }
                )
            }

            // Divider
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. Onsen Plan Management Section (Trip-specific)
            item {
                Text(
                    text = "温泉計画 (選択中の旅行: ${selectedPlan?.title ?: "未選択"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (selectedPlan == null) {
                item {
                    NoTripSelectedOnsenState()
                }
            } else {
                if (onsenPlans.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "温泉計画がありません",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        // Initialize empty plans for each member
                                        members.forEach { m ->
                                            viewModel.saveOnsenPlan(
                                                OnsenPlan(
                                                    travelId = selectedPlan!!.id,
                                                    memberCode = m.code,
                                                    totalTimes = 0,
                                                    typeATimes = 0,
                                                    typeBTimes = 0
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Text("温泉計画を開始する")
                                }
                            }
                        }
                    }
                } else {
                    items(onsenPlans) { plan ->
                        val memberName = members.find { it.code == plan.memberCode }?.name ?: "不明"
                        OnsenPlanCard(
                            plan = plan,
                            memberName = memberName,
                            onEditClick = { editingOnsenPlan = plan }
                        )
                    }
                }
            }
        }
    }

    // Edit Member Dialog
    if (editingMember != null) {
        MemberEditDialog(
            member = editingMember!!,
            onDismiss = { editingMember = null },
            onSave = { updated ->
                viewModel.updateMember(updated)
                editingMember = null
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        MemberAddDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, code ->
                viewModel.updateMember(Member(code = code, name = name))
                showAddMemberDialog = false
            }
        )
    }

    // Edit Onsen Plan Dialog
    if (editingOnsenPlan != null) {
        val memberName = members.find { it.code == editingOnsenPlan!!.memberCode }?.name ?: "不明"
        OnsenPlanEditDialog(
            plan = editingOnsenPlan!!,
            memberName = memberName,
            onDismiss = { editingOnsenPlan = null },
            onSave = { updated ->
                viewModel.saveOnsenPlan(updated)
                editingOnsenPlan = null
            }
        )
    }
}

@Composable
fun MemberRowCard(
    member: Member,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.code.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "メンバー識別コード: ${member.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "編集", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun OnsenPlanCard(
    plan: OnsenPlan,
    memberName: String,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .testTag("onsen_plan_card_${plan.memberCode}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HotTub,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = memberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_onsen_${plan.memberCode}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "温泉計画を編集", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bath details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "入浴合計",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${plan.totalTimes} 回",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Type A
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "タイプA (露天・大浴場等)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${plan.typeATimes} 回",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Type B
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "タイプB (展望・部屋風呂等)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${plan.typeBTimes} 回",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (plan.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = plan.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoTripSelectedOnsenState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.HotTub,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "旅行プランが選択されていません",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "「旅行一覧」で旅行を選択すると、その旅独自の温泉計画を管理できます。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MemberEditDialog(
    member: Member,
    onDismiss: () -> Unit,
    onSave: (Member) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("メンバーの編集", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("メンバー名 *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(member.copy(name = name)) },
                enabled = name.isNotBlank()
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

@Composable
fun MemberAddDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("メンバーの新規追加", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("メンバー名 *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("コード (1文字, 例: d, e) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && code.isNotBlank()) onSave(name, code.take(2).lowercase()) },
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun OnsenPlanEditDialog(
    plan: OnsenPlan,
    memberName: String,
    onDismiss: () -> Unit,
    onSave: (OnsenPlan) -> Unit
) {
    var totalTimes by remember { mutableStateOf(plan.totalTimes) }
    var typeATimes by remember { mutableStateOf(plan.typeATimes) }
    var typeBTimes by remember { mutableStateOf(plan.typeBTimes) }
    var note by remember { mutableStateOf(plan.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$memberName の温浴計画", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Count Row with Increment/Decrement Buttons
                Column {
                    Text("入浴合計回数: $totalTimes 回", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (totalTimes > 0) totalTimes-- }) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = "減少", tint = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = totalTimes.toFloat(),
                            onValueChange = { totalTimes = it.toInt() },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { if (totalTimes < 10) totalTimes++ }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "増加", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Type A
                Column {
                    Text("タイプA (大浴場等): $typeATimes 回", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { if (typeATimes > 0) typeATimes-- }) { Text("-") }
                        Text(text = "$typeATimes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Button(onClick = { if (typeATimes < 10) typeATimes++ }) { Text("+") }
                    }
                }

                // Type B
                Column {
                    Text("タイプB (展望風呂等): $typeBTimes 回", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { if (typeBTimes > 0) typeBTimes-- }) { Text("-") }
                        Text(text = "$typeBTimes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Button(onClick = { if (typeBTimes < 10) typeBTimes++ }) { Text("+") }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("温泉計画メモ (例: 体調優先)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        plan.copy(
                            totalTimes = totalTimes,
                            typeATimes = typeATimes,
                            typeBTimes = typeBTimes,
                            note = note
                        )
                    )
                }
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
