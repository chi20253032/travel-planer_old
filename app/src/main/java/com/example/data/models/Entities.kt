package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey val code: String, // "a", "b", "c" etc.
    val name: String
)

@Entity(tableName = "travel_plans")
data class TravelPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val destinations: String,
    val year: String,
    val days: String,
    val transport: String,
    val accommodation: String,
    val reservationStatus: String, // "予約済み", "未予約", "未定"
    val memo: String,
    val startDateString: String = "", // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "onsen_plans")
data class OnsenPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val travelId: Int,
    val memberCode: String, // "a", "b", "c"
    val totalTimes: Int,
    val typeATimes: Int,
    val typeBTimes: Int,
    val note: String = ""
)

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val travelId: Int,
    val name: String,
    val isChecked: Boolean = false
)

@Entity(tableName = "travel_budgets")
data class TravelBudget(
    @PrimaryKey val travelId: Int, // 1-to-1 with travel plan
    val transportCost: Long = 0,
    val accommodationCost: Long = 0,
    val foodCost: Long = 0,
    val souvenirCost: Long = 0,
    val otherCost: Long = 0
)

@Entity(tableName = "travel_photos")
data class TravelPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val travelId: Int,
    val photoUri: String // Can be a local URI, path, or simulated URI/resource
)

@Entity(tableName = "timeline_items")
data class TimelineItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val travelId: Int,
    val day: Int, // e.g. 1 for Day 1
    val time: String, // e.g. "10:00"
    val activity: String,
    val note: String = ""
)
