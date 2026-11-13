package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.models.*
import com.example.data.repository.TravelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TravelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TravelRepository
    val isDarkMode = MutableStateFlow(true) // Default to elegant dark theme!

    val allTravelPlans: StateFlow<List<TravelPlan>>
    val allMembers: StateFlow<List<Member>>

    val selectedTravelId = MutableStateFlow<Int?>(null)

    val selectedTravelPlan: StateFlow<TravelPlan?>
    val selectedOnsenPlans: StateFlow<List<OnsenPlan>>
    val selectedChecklist: StateFlow<List<ChecklistItem>>
    val selectedBudget: StateFlow<TravelBudget?>
    val selectedPhotos: StateFlow<List<TravelPhoto>>
    val selectedTimeline: StateFlow<List<TimelineItem>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TravelRepository(database.travelDao())

        // Fetch primary lists
        allTravelPlans = repository.allTravelPlans
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allMembers = repository.allMembers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initialize DB asynchronously on startup
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
            // Default select the first trip if available
            val firstPlan = repository.allTravelPlans.first().firstOrNull()
            if (firstPlan != null && selectedTravelId.value == null) {
                selectedTravelId.value = firstPlan.id
            }
        }

        // Dynamically track selected trip details
        selectedTravelPlan = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getTravelPlanById(id) else flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        selectedOnsenPlans = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getOnsenPlansForTravel(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        selectedChecklist = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getChecklistForTravel(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        selectedBudget = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getBudgetForTravel(id) else flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        selectedPhotos = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getPhotosForTravel(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        selectedTimeline = selectedTravelId
            .flatMapLatest { id ->
                if (id != null) repository.getTimelineForTravel(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun selectTravel(id: Int) {
        selectedTravelId.value = id
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    // --- Core Operations ---

    // 1. Travel Plan Operations
    fun addTravelPlan(
        title: String,
        destinations: String,
        year: String,
        days: String,
        transport: String,
        accommodation: String,
        reservationStatus: String,
        memo: String,
        startDateString: String
    ) {
        viewModelScope.launch {
            val newPlan = TravelPlan(
                title = title,
                destinations = destinations,
                year = year,
                days = days,
                transport = transport,
                accommodation = accommodation,
                reservationStatus = reservationStatus,
                memo = memo,
                startDateString = startDateString
            )
            val newId = repository.insertTravelPlan(newPlan)
            // Select the newly added trip
            selectTravel(newId)
        }
    }

    fun updateTravelPlan(plan: TravelPlan) {
        viewModelScope.launch {
            repository.updateTravelPlan(plan)
        }
    }

    fun deleteTravelPlan(plan: TravelPlan) {
        viewModelScope.launch {
            repository.deleteTravelPlan(plan)
            // Auto-select another plan if any are left
            val plans = allTravelPlans.value.filter { it.id != plan.id }
            if (plans.isNotEmpty()) {
                selectTravel(plans.first().id)
            } else {
                selectedTravelId.value = null
            }
        }
    }

    // 2. Member Operations
    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.insertMember(member)
        }
    }

    // 3. Onsen Plan Operations
    fun saveOnsenPlan(plan: OnsenPlan) {
        viewModelScope.launch {
            repository.insertOnsenPlan(plan)
        }
    }

    fun updateOnsenPlan(plan: OnsenPlan) {
        viewModelScope.launch {
            repository.updateOnsenPlan(plan)
        }
    }

    // 4. Checklist Operations
    fun toggleChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.updateChecklistItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun addChecklistItem(name: String) {
        val travelId = selectedTravelId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertChecklistItem(ChecklistItem(travelId = travelId, name = name))
        }
    }

    fun deleteChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    // 5. Budget Operations
    fun updateBudget(
        transportCost: Long,
        accommodationCost: Long,
        foodCost: Long,
        souvenirCost: Long,
        otherCost: Long
    ) {
        val travelId = selectedTravelId.value ?: return
        viewModelScope.launch {
            repository.updateBudget(
                TravelBudget(
                    travelId = travelId,
                    transportCost = transportCost,
                    accommodationCost = accommodationCost,
                    foodCost = foodCost,
                    souvenirCost = souvenirCost,
                    otherCost = otherCost
                )
            )
        }
    }

    // 6. Photo Operations
    fun addPhoto(photoUri: String) {
        val travelId = selectedTravelId.value ?: return
        viewModelScope.launch {
            repository.insertPhoto(TravelPhoto(travelId = travelId, photoUri = photoUri))
        }
    }

    fun deletePhoto(photo: TravelPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
        }
    }

    // 7. Timeline Operations
    fun addTimelineItem(day: Int, time: String, activity: String, note: String) {
        val travelId = selectedTravelId.value ?: return
        viewModelScope.launch {
            repository.insertTimelineItem(
                TimelineItem(
                    travelId = travelId,
                    day = day,
                    time = time,
                    activity = activity,
                    note = note
                )
            )
        }
    }

    fun updateTimelineItem(item: TimelineItem) {
        viewModelScope.launch {
            repository.updateTimelineItem(item)
        }
    }

    fun deleteTimelineItem(item: TimelineItem) {
        viewModelScope.launch {
            repository.deleteTimelineItem(item)
        }
    }
}
