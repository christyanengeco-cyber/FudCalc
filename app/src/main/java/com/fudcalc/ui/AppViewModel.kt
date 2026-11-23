package com.fudcalc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fudcalc.data.ServiceLocator
import com.fudcalc.data.FoodRepository
import com.fudcalc.domain.food.Food
import com.fudcalc.domain.meals.DiaryEntry
import com.fudcalc.domain.meals.MealType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val foodRepository = ServiceLocator.foodRepository(application)
    private val diaryRepository = ServiceLocator.diaryRepository(application)

    private val selectedDate = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = selectedDate.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery.asStateFlow()

    val entries: StateFlow<List<DiaryEntry>> = selectedDate
        .flatMapLatest { diaryRepository.entries(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<List<Food>> = searchQuery
        .flatMapLatest { foodRepository.search(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val packageInfo: StateFlow<FoodRepository.PackageInfo?> = foodRepository.packageInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val importFailure = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = importFailure.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { foodRepository.ensureImported() }
                .onFailure { importFailure.value = it.message ?: it::class.simpleName }
        }
    }

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    fun previousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
    }

    fun addEntry(food: Food, grams: Double, meal: MealType) {
        viewModelScope.launch { diaryRepository.add(food, grams, meal, selectedDate.value) }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch { diaryRepository.delete(entryId) }
    }
}
