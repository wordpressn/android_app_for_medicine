package com.example.medireminder.data

import java.time.LocalDate

enum class Period { MORNING, NOON, NIGHT }

data class Medicine(
    val id: String,
    val brandName: String,
    val genericName: String,
    val dose: String,
    val foodTiming: String,
    val periods: Set<Period>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val imageUrl: String = ""
)

data class AdherenceLog(
    val date: LocalDate,
    val period: Period,
    val completed: Boolean,
    val completedAtEpochMs: Long
)
