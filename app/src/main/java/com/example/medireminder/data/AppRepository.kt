package com.example.medireminder.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class AppRepository {
    private val today = LocalDate.now()

    private val _medicines = MutableStateFlow(
        listOf(
            Medicine(
                id = "1",
                brandName = "Telma",
                genericName = "Telmisartan",
                dose = "1 tablet (40mg)",
                foodTiming = "After food",
                periods = setOf(Period.MORNING),
                startDate = today.minusDays(5),
                endDate = today.plusDays(20)
            ),
            Medicine(
                id = "2",
                brandName = "Glycomet",
                genericName = "Metformin",
                dose = "1 tablet (500mg)",
                foodTiming = "After food",
                periods = setOf(Period.NOON, Period.NIGHT),
                startDate = today.minusDays(2),
                endDate = today.plusDays(15)
            )
        )
    )

    private val _adherence = MutableStateFlow(mapOf<Pair<LocalDate, Period>, AdherenceLog>())

    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()
    val adherence: StateFlow<Map<Pair<LocalDate, Period>, AdherenceLog>> = _adherence.asStateFlow()

    fun medicinesForPeriod(period: Period, date: LocalDate = LocalDate.now()): List<Medicine> {
        return _medicines.value.filter {
            period in it.periods && !date.isBefore(it.startDate) && !date.isAfter(it.endDate)
        }
    }

    fun completePeriod(period: Period, date: LocalDate = LocalDate.now()) {
        val key = date to period
        _adherence.value = _adherence.value + (
            key to AdherenceLog(date, period, true, System.currentTimeMillis())
        )
    }

    fun isCompleted(period: Period, date: LocalDate = LocalDate.now()): Boolean {
        return _adherence.value[date to period]?.completed == true
    }

    fun addMedicine(medicine: Medicine) {
        _medicines.value = _medicines.value + medicine
    }

    fun deleteMedicine(id: String) {
        _medicines.value = _medicines.value.filterNot { it.id == id }
    }
}
