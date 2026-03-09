package com.example.medireminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medireminder.data.Period
import com.example.medireminder.ui.AppViewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm = remember { AppViewModel() }
                var screen by remember { mutableStateOf("role") }

                when (screen) {
                    "role" -> RoleSelection(
                        onParent = { screen = "parent" },
                        onAdmin = { screen = "admin" }
                    )
                    "parent" -> ParentHome(vm, onAdmin = { screen = "admin" })
                    else -> AdminScreen(vm, onParent = { screen = "parent" })
                }
            }
        }
    }
}

@Composable
private fun RoleSelection(onParent: () -> Unit, onAdmin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MediReminder", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onParent, modifier = Modifier.fillMaxWidth()) { Text("Open as Parent") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Open as Son/Daughter (Admin)") }
    }
}

@Composable
private fun ParentHome(vm: AppViewModel, onAdmin: () -> Unit) {
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail) {
        PeriodDetailScreen(vm, onBack = { showDetail = false })
        return
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Today\'s Medicine", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Select period", fontSize = 18.sp)
        Spacer(Modifier.height(18.dp))

        Period.values().forEach { period ->
            val completed = vm.isPeriodCompleted(period)
            val colors = periodGradient(period)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(period.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 24.sp, color = Color.White)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (completed) "Completed" else "Pending", color = Color.White)
                        OutlinedButton(onClick = {
                            vm.selectPeriod(period)
                            showDetail = true
                        }) { Text("Open") }
                    }
                }
            }
        }

        OutlinedButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) {
            Text("Go to Admin")
        }
    }
}

@Composable
private fun PeriodDetailScreen(vm: AppViewModel, onBack: () -> Unit) {
    val meds = vm.currentPeriodMedicines()
    val index = vm.periodIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("${vm.selectedPeriod.name} medicines", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (meds.isEmpty()) {
                Text("No active medicine for this period.")
            } else {
                val med = meds[index]
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(med.brandName, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text(med.genericName, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Dose: ${med.dose}")
                        Text("Food timing: ${med.foodTiming}")
                        Text("Active until: ${med.endDate}")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    OutlinedButton(onClick = vm::previousMedicine, enabled = index > 0) { Text("Previous") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = vm::nextMedicine, enabled = index < meds.lastIndex) { Text("Next") }
                }
            }
        }

        Column {
            Button(onClick = {
                vm.completeSelectedPeriod()
                onBack()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("I have completed the medicine for the selected period")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

@Composable
private fun AdminScreen(vm: AppViewModel, onParent: () -> Unit) {
    val medicineList by vm.medicines.collectAsState()

    var brand by remember { mutableStateOf("") }
    var generic by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Admin - Manage Medicines", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(brand, { brand = it }, label = { Text("Brand name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(generic, { generic = it }, label = { Text("Generic name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(dose, { dose = it }, label = { Text("Dose") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (brand.isNotBlank()) {
                vm.addMedicine(
                    brandName = brand,
                    genericName = generic,
                    dose = dose.ifBlank { "1 tablet" },
                    foodTiming = "After food",
                    periods = setOf(Period.MORNING),
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(15)
                )
                brand = ""
                generic = ""
                dose = ""
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Medicine (Morning default)")
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(medicineList) { med ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(med.brandName, fontWeight = FontWeight.Bold)
                            Text(med.genericName, fontSize = 12.sp)
                            Text("${med.periods.joinToString()} | ${med.startDate} - ${med.endDate}", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = { vm.deleteMedicine(med.id) }) { Text("Delete") }
                    }
                }
            }
        }

        OutlinedButton(onClick = onParent, modifier = Modifier.fillMaxWidth()) {
            Text("Go to Parent")
        }
    }
}

private fun periodGradient(period: Period): List<Color> = when (period) {
    Period.MORNING -> listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
    Period.NOON -> listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6))
    Period.NIGHT -> listOf(Color(0xFF283593), Color(0xFF1A237E))
}
