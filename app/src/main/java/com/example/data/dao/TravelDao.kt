package com.example.data.dao

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {

    // Travel Plans
    @Query("SELECT * FROM travel_plans ORDER BY id ASC")
    fun getAllTravelPlans(): Flow<List<TravelPlan>>

    @Query("SELECT * FROM travel_plans WHERE id = :id LIMIT 1")
    fun getTravelPlanById(id: Int): Flow<TravelPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelPlan(plan: TravelPlan): Long

    @Update
    suspend fun updateTravelPlan(plan: TravelPlan)

    @Delete
    suspend fun deleteTravelPlan(plan: TravelPlan)


    // Members
    @Query("SELECT * FROM members ORDER BY code ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)


    // Onsen Plans
    @Query("SELECT * FROM onsen_plans WHERE travelId = :travelId")
    fun getOnsenPlansForTravel(travelId: Int): Flow<List<OnsenPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnsenPlan(plan: OnsenPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnsenPlans(plans: List<OnsenPlan>)

    @Update
    suspend fun updateOnsenPlan(plan: OnsenPlan)

    @Delete
    suspend fun deleteOnsenPlan(plan: OnsenPlan)


    // Checklist Items
    @Query("SELECT * FROM checklist_items WHERE travelId = :travelId ORDER BY id ASC")
    fun getChecklistForTravel(travelId: Int): Flow<List<ChecklistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItem)


    // Travel Budget
    @Query("SELECT * FROM travel_budgets WHERE travelId = :travelId LIMIT 1")
    fun getBudgetForTravel(travelId: Int): Flow<TravelBudget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: TravelBudget)

    @Update
    suspend fun updateBudget(budget: TravelBudget)


    // Travel Photos
    @Query("SELECT * FROM travel_photos WHERE travelId = :travelId")
    fun getPhotosForTravel(travelId: Int): Flow<List<TravelPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: TravelPhoto)

    @Delete
    suspend fun deletePhoto(photo: TravelPhoto)


    // Timeline Items
    @Query("SELECT * FROM timeline_items WHERE travelId = :travelId ORDER BY day ASC, time ASC")
    fun getTimelineForTravel(travelId: Int): Flow<List<TimelineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItem(item: TimelineItem)

    @Update
    suspend fun updateTimelineItem(item: TimelineItem)

    @Delete
    suspend fun deleteTimelineItem(item: TimelineItem)
}
