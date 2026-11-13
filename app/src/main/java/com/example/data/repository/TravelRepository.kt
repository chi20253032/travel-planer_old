package com.example.data.repository

import com.example.data.dao.TravelDao
import com.example.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class TravelRepository(private val travelDao: TravelDao) {

    val allTravelPlans: Flow<List<TravelPlan>> = travelDao.getAllTravelPlans()
    val allMembers: Flow<List<Member>> = travelDao.getAllMembers()

    fun getTravelPlanById(id: Int): Flow<TravelPlan?> = travelDao.getTravelPlanById(id)
    fun getOnsenPlansForTravel(travelId: Int): Flow<List<OnsenPlan>> = travelDao.getOnsenPlansForTravel(travelId)
    fun getChecklistForTravel(travelId: Int): Flow<List<ChecklistItem>> = travelDao.getChecklistForTravel(travelId)
    fun getBudgetForTravel(travelId: Int): Flow<TravelBudget?> = travelDao.getBudgetForTravel(travelId)
    fun getPhotosForTravel(travelId: Int): Flow<List<TravelPhoto>> = travelDao.getPhotosForTravel(travelId)
    fun getTimelineForTravel(travelId: Int): Flow<List<TimelineItem>> = travelDao.getTimelineForTravel(travelId)

    // Check if empty and initialize with default data
    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingPlans = travelDao.getAllTravelPlans().first()
        if (existingPlans.isEmpty()) {
            // 1. Insert Members
            val members = listOf(
                Member(code = "a", name = "大人女性"),
                Member(code = "b", name = "大人男性"),
                Member(code = "c", name = "男の子（子ども）")
            )
            for (member in members) {
                travelDao.insertMember(member)
            }

            // 2. Insert Travel Plans
            val trip1 = TravelPlan(
                title = "姫路旅行",
                destinations = "姫路セントラルパーク, ホテルモントレ姫路",
                year = "2026年",
                days = "1泊2日",
                transport = "電車",
                accommodation = "ホテルモントレ姫路",
                reservationStatus = "予約済み",
                memo = "今年の旅行。",
                startDateString = "2026-10-15"
            )
            val trip1Id = travelDao.insertTravelPlan(trip1).toInt()

            val trip2 = TravelPlan(
                title = "Premium 箕面観光ホテル",
                destinations = "Premium 箕面観光ホテル",
                year = "2027年（予定）",
                days = "1泊2日",
                transport = "電車",
                accommodation = "Premium 箕面観光ホテル",
                reservationStatus = "未予約",
                memo = "リニューアル後のホテルを利用予定。\nミライ人間洗濯機も体験したい。",
                startDateString = "2027-02-14"
            )
            val trip2Id = travelDao.insertTravelPlan(trip2).toInt()

            val trip3 = TravelPlan(
                title = "GREEN×EXPO 2027（花博）",
                destinations = "GREEN×EXPO 2027",
                year = "2027年",
                days = "未定",
                transport = "未定",
                accommodation = "未定",
                reservationStatus = "未予約",
                memo = "花博の終盤ごろに行く予定。",
                startDateString = "2027-09-10"
            )
            val trip3Id = travelDao.insertTravelPlan(trip3).toInt()

            val trip4 = TravelPlan(
                title = "伊勢・志摩・鳥羽旅行",
                destinations = "伊勢・志摩・鳥羽",
                year = "未定",
                days = "未定",
                transport = "未定",
                accommodation = "未定",
                reservationStatus = "未予約",
                memo = "また行きたい旅行。",
                startDateString = ""
            )
            val trip4Id = travelDao.insertTravelPlan(trip4).toInt()

            // List of all travel IDs
            val travelIds = listOf(trip1Id, trip2Id, trip3Id, trip4Id)

            // 3. Insert initial Onsen plan (specifically for Premium Minoh Tourist Hotel, Trip 2)
            val onsenPlans = listOf(
                OnsenPlan(travelId = trip2Id, memberCode = "a", totalTimes = 2, typeATimes = 1, typeBTimes = 1),
                OnsenPlan(travelId = trip2Id, memberCode = "b", totalTimes = 4, typeATimes = 2, typeBTimes = 2),
                OnsenPlan(travelId = trip2Id, memberCode = "c", totalTimes = 4, typeATimes = 2, typeBTimes = 2, note = "体調優先")
            )
            travelDao.insertOnsenPlans(onsenPlans)

            // Initialize other trips with empty onsen plans if needed, or we dynamically allow adding them!
            // Let's also pre-populate default empty onsen plans for Himeji (Trip 1) just so they see it
            val onsenPlansTrip1 = listOf(
                OnsenPlan(travelId = trip1Id, memberCode = "a", totalTimes = 0, typeATimes = 0, typeBTimes = 0),
                OnsenPlan(travelId = trip1Id, memberCode = "b", totalTimes = 0, typeATimes = 0, typeBTimes = 0),
                OnsenPlan(travelId = trip1Id, memberCode = "c", totalTimes = 0, typeATimes = 0, typeBTimes = 0)
            )
            travelDao.insertOnsenPlans(onsenPlansTrip1)

            // 4. Insert Belongings Check List for EVERY travel plan
            val defaultBelongings = listOf(
                "財布", "スマホ", "充電器", "モバイルバッテリー", "着替え", "タオル", "歯ブラシ", "保険証", "常備薬"
            )
            for (tid in travelIds) {
                val checklistItems = defaultBelongings.map { name ->
                    ChecklistItem(travelId = tid, name = name, isChecked = false)
                }
                travelDao.insertChecklistItems(checklistItems)
            }

            // 5. Insert budgets for each travel plan
            for (tid in travelIds) {
                travelDao.insertBudget(
                    TravelBudget(
                        travelId = tid,
                        transportCost = 0,
                        accommodationCost = 0,
                        foodCost = 0,
                        souvenirCost = 0,
                        otherCost = 0
                    )
                )
            }

            // 6. Pre-populate default timeline items
            // Trip 1 timeline
            travelDao.insertTimelineItem(TimelineItem(travelId = trip1Id, day = 1, time = "10:00", activity = "電車で出発！"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip1Id, day = 1, time = "12:00", activity = "ホテルモントレ姫路に荷物を預ける"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip1Id, day = 1, time = "13:00", activity = "姫路セントラルパーク到着！サファリ見学", note = "ホワイトライオンに会う"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip1Id, day = 2, time = "10:00", activity = "姫路城を見学"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip1Id, day = 2, time = "16:00", activity = "お土産を買って電車で帰路へ"))

            // Trip 2 timeline
            travelDao.insertTimelineItem(TimelineItem(travelId = trip2Id, day = 1, time = "14:00", activity = "Premium 箕面観光ホテル チェックイン"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip2Id, day = 1, time = "15:00", activity = "温泉（タイプA）1回目", note = "絶景の露天風呂"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip2Id, day = 1, time = "17:00", activity = "ミライ人間洗濯機体験！"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip2Id, day = 1, time = "19:00", activity = "豪華夕食バイキング"))
            travelDao.insertTimelineItem(TimelineItem(travelId = trip2Id, day = 2, time = "07:00", activity = "朝の温泉（タイプB）2回目"))
        }
    }

    // Travel Plan operations
    suspend fun insertTravelPlan(plan: TravelPlan): Int = withContext(Dispatchers.IO) {
        val id = travelDao.insertTravelPlan(plan).toInt()
        // Initialize budget
        travelDao.insertBudget(TravelBudget(travelId = id))
        // Initialize checklist
        val defaultBelongings = listOf(
            "財布", "スマホ", "充電器", "モバイルバッテリー", "着替え", "タオル", "歯ブラシ", "保険証", "常備薬"
        )
        val checklistItems = defaultBelongings.map { name ->
            ChecklistItem(travelId = id, name = name, isChecked = false)
        }
        travelDao.insertChecklistItems(checklistItems)
        id
    }

    suspend fun updateTravelPlan(plan: TravelPlan) = withContext(Dispatchers.IO) {
        travelDao.updateTravelPlan(plan)
    }

    suspend fun deleteTravelPlan(plan: TravelPlan) = withContext(Dispatchers.IO) {
        travelDao.deleteTravelPlan(plan)
    }

    // Member operations
    suspend fun insertMember(member: Member) = withContext(Dispatchers.IO) {
        travelDao.insertMember(member)
    }

    suspend fun deleteMember(member: Member) = withContext(Dispatchers.IO) {
        travelDao.deleteMember(member)
    }

    // Onsen operations
    suspend fun insertOnsenPlan(plan: OnsenPlan) = withContext(Dispatchers.IO) {
        travelDao.insertOnsenPlan(plan)
    }

    suspend fun updateOnsenPlan(plan: OnsenPlan) = withContext(Dispatchers.IO) {
        travelDao.updateOnsenPlan(plan)
    }

    suspend fun deleteOnsenPlan(plan: OnsenPlan) = withContext(Dispatchers.IO) {
        travelDao.deleteOnsenPlan(plan)
    }

    // Checklist operations
    suspend fun insertChecklistItem(item: ChecklistItem) = withContext(Dispatchers.IO) {
        travelDao.insertChecklistItem(item)
    }

    suspend fun updateChecklistItem(item: ChecklistItem) = withContext(Dispatchers.IO) {
        travelDao.updateChecklistItem(item)
    }

    suspend fun deleteChecklistItem(item: ChecklistItem) = withContext(Dispatchers.IO) {
        travelDao.deleteChecklistItem(item)
    }

    // Budget operations
    suspend fun updateBudget(budget: TravelBudget) = withContext(Dispatchers.IO) {
        travelDao.updateBudget(budget)
    }

    // Photo operations
    suspend fun insertPhoto(photo: TravelPhoto) = withContext(Dispatchers.IO) {
        travelDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: TravelPhoto) = withContext(Dispatchers.IO) {
        travelDao.deletePhoto(photo)
    }

    // Timeline operations
    suspend fun insertTimelineItem(item: TimelineItem) = withContext(Dispatchers.IO) {
        travelDao.insertTimelineItem(item)
    }

    suspend fun updateTimelineItem(item: TimelineItem) = withContext(Dispatchers.IO) {
        travelDao.updateTimelineItem(item)
    }

    suspend fun deleteTimelineItem(item: TimelineItem) = withContext(Dispatchers.IO) {
        travelDao.deleteTimelineItem(item)
    }
}
