package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.OnsenScreen
import com.example.ui.screens.PackingBudgetScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.screens.TripsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TravelViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: TravelViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isDarkMode) {
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            NavigationBar(
              modifier = Modifier
                .navigationBarsPadding()
                .testTag("main_navigation_bar"),
              tonalElevation = 8.dp
            ) {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.CardTravel, contentDescription = "旅行一覧") },
                label = { Text("旅行一覧") },
                modifier = Modifier.testTag("tab_trips")
              )
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.Schedule, contentDescription = "タイムライン") },
                label = { Text("タイムライン") },
                modifier = Modifier.testTag("tab_timeline")
              )
              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.HotTub, contentDescription = "温泉・家族") },
                label = { Text("温泉・家族") },
                modifier = Modifier.testTag("tab_onsen")
              )
              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Assignment, contentDescription = "持ち物・予算") },
                label = { Text("持ち物・予算") },
                modifier = Modifier.testTag("tab_packing_budget")
              )
            }
          },
          containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
          // Ambient background decoration
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = if (isDarkMode) {
                    listOf(
                      MaterialTheme.colorScheme.background,
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                      MaterialTheme.colorScheme.background
                    )
                  } else {
                    listOf(
                      MaterialTheme.colorScheme.background,
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                      MaterialTheme.colorScheme.background
                    )
                  }
                )
              )
              .padding(bottom = innerPadding.calculateBottomPadding())
          ) {
            when (selectedTab) {
              0 -> TripsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
              1 -> TimelineScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
              2 -> OnsenScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
              3 -> PackingBudgetScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
          }
        }
      }
    }
  }
}

