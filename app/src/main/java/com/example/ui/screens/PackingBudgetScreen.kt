package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChecklistItem
import com.example.data.models.TravelBudget
import com.example.ui.viewmodel.TravelViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingBudgetScreen(
    viewModel: TravelViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPlan by viewModel.selectedTravelPlan.collectAsState()
    val checklist by viewModel.selectedChecklist.collectAsState()
    val budget by viewModel.selectedBudget.collectAsState()

    var newItemName by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "持ち物 & 予算管理",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "旅行の荷物準備と旅費の自動計算",
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
        if (selectedPlan == null) {
            NoTripSelectedPackingState()
        } else {
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
                // 1. Budget Card Section
                item {
                    Text(
                        text = "予算・旅費管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    BudgetOverviewCard(
                        budget = budget ?: TravelBudget(travelId = selectedPlan!!.id),
                        onBudgetUpdate = { trans, accom, food, sauv, other ->
                            viewModel.updateBudget(trans, accom, food, sauv, other)
                        }
                    )
                }

                // Divider
                item {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }

                // 2. Checklist Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "持ち物チェックリスト",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        // Progress indicator
                        val checkedCount = checklist.count { it.isChecked }
                        val totalCount = checklist.size
                        if (totalCount > 0) {
                            Text(
                                text = "準備率: $checkedCount/$totalCount (${(checkedCount * 100) / totalCount}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Add Checklist Item field
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            placeholder = { Text("例: カメラ、折りたたみ傘") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_item_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = {
                                if (newItemName.isNotBlank()) {
                                    viewModel.addChecklistItem(newItemName)
                                    newItemName = ""
                                }
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("add_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "追加")
                        }
                    }
                }

                // Packing List
                if (checklist.isEmpty()) {
                    item {
                        Text(
                            text = "持ち物がありません。上の入力欄から持ち物を追加しましょう。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(checklist) { item ->
                        ChecklistItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecklistItem(item) },
                            onDelete = { viewModel.deleteChecklistItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetOverviewCard(
    budget: TravelBudget,
    onBudgetUpdate: (
        transportCost: Long,
        accommodationCost: Long,
        foodCost: Long,
        souvenirCost: Long,
        otherCost: Long
    ) -> Unit
) {
    val totalAmount = budget.transportCost + budget.accommodationCost + budget.foodCost + budget.souvenirCost + budget.otherCost
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    var editingCategory by remember { mutableStateOf<String?>(null) }
    var inputAmountStr by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Total display with nice gradient back panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "予算合計金額",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(totalAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            val categories = listOf(
                Triple("交通費", budget.transportCost, Icons.Default.DirectionsCar),
                Triple("宿泊費", budget.accommodationCost, Icons.Default.Hotel),
                Triple("食事代", budget.foodCost, Icons.Default.Restaurant),
                Triple("お土産代", budget.souvenirCost, Icons.Default.CardGiftcard),
                Triple("その他", budget.otherCost, Icons.Default.ConfirmationNumber)
            )

            categories.forEach { (label, amount, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            editingCategory = label
                            inputAmountStr = amount.toString()
                        }
                        .testTag("budget_item_$label"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencyFormatter.format(amount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "編集",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Edit Category Dialog
    if (editingCategory != null) {
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text(text = "${editingCategory} の編集", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "金額を入力してください:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = inputAmountStr,
                        onValueChange = { inputAmountStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("円") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedVal = inputAmountStr.toLongOrNull() ?: 0L
                        val trans = if (editingCategory == "交通費") parsedVal else budget.transportCost
                        val accom = if (editingCategory == "宿泊費") parsedVal else budget.accommodationCost
                        val food = if (editingCategory == "食事代") parsedVal else budget.foodCost
                        val sauv = if (editingCategory == "お土産代") parsedVal else budget.souvenirCost
                        val other = if (editingCategory == "その他") parsedVal else budget.otherCost

                        onBudgetUpdate(trans, accom, food, sauv, other)
                        editingCategory = null
                    },
                    modifier = Modifier.testTag("dialog_amount_save")
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (item.isChecked) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("checklist_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).clickable(onClick = onToggle)
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("checklist_checkbox_${item.id}")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_item_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NoTripSelectedPackingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CardTravel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "旅行プランが選択されていません",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "「旅行一覧」タブで旅行プランを選択して、荷物リストと予算管理を開始しましょう。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
