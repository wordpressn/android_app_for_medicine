package com.example.medireminder.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.medireminder.data.AppRepository
import com.example.medireminder.data.Medicine
import com.example.medireminder.data.Period
import java.time.LocalDate
import java.util.UUID

class AppViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    var selectedPeriod by mutableStateOf(Period.MORNING)
        private set

    var periodIndex by mutableStateOf(0)
        private set

    val medicines get() = repository.medicines

    fun selectPeriod(period: Period) {
        selectedPeriod = period
        periodIndex = 0
    }

    fun currentPeriodMedicines(): List<Medicine> = repository.medicinesForPeriod(selectedPeriod, LocalDate.now())

    fun nextMedicine() {
        val size = currentPeriodMedicines().size
        if (size > 0 && periodIndex < size - 1) periodIndex++
    }

    fun previousMedicine() {
        if (periodIndex > 0) periodIndex--
    }

    fun completeSelectedPeriod() = repository.completePeriod(selectedPeriod)

    fun isPeriodCompleted(period: Period): Boolean = repository.isCompleted(period)

    fun addMedicine(
        brandName: String,
        genericName: String,
        dose: String,
        foodTiming: String,
        periods: Set<Period>,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        repository.addMedicine(
            Medicine(
                id = UUID.randomUUID().toString(),
                brandName = brandName,
                genericName = genericName,
                dose = dose,
                foodTiming = foodTiming,
                periods = periods,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    fun deleteMedicine(id: String) = repository.deleteMedicine(id)
}
