package com.cmc.alarminicompose.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.cmc.alarminicompose.ui.theme.AlarmClockTheme
import kotlinx.coroutines.delay
import java.util.*
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "clock") {
                composable("clock") { ClockScreen(navController) }
                composable("set_alarm") { SetAlarmScreen(navController) }
            }
        }
    }
}

@Composable
fun ClockScreen(navController: NavController) {
    var hour by remember { mutableStateOf("0") }
    var minute by remember { mutableStateOf("0") }
    var second by remember { mutableStateOf("0") }
    var amOrPm by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR).run {
                if (this.toString().length == 1) "0$this" else "$this"
            }
            minute = cal.get(Calendar.MINUTE).run {
                if (this.toString().length == 1) "0$this" else "$this"
            }
            second = cal.get(Calendar.SECOND).run {
                if (this.toString().length == 1) "0$this" else "$this"
            }
            amOrPm = cal.get(Calendar.AM_PM).run {
                if (this == Calendar.AM) "AM" else "PM"
            }

            delay(1000)
        }
    }

    AlarmClockTheme {
        Scaffold(bottomBar = { NavigationBarComponent(navController) }) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HeaderComponent()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight(fraction = 0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // AnalogClockComponent would go here (custom clock)
                            DigitalClockComponent(
                                hour = hour,
                                minute = minute,
                                amOrPm = amOrPm,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { navController.navigate("set_alarm") }) {
                                Text("Set Alarm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderComponent() {
    Box(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(text = "Clock", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun NavigationBarComponent(navController: NavController) {
    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
    ) {
        NavigationBarItem(icon = { Icon(Icons.Default.Add, contentDescription = null) }, selected = false, onClick = { navController.navigate("set_alarm") })
    }
}

@Composable
fun DigitalClockComponent(
    hour: String,
    minute: String,
    amOrPm: String,
) {
    Text(text = "$hour:$minute $amOrPm", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "Morocco, Rabat",
        style = MaterialTheme.typography.bodyMedium
    )
}

// Set Alarm Screen
@Composable
fun SetAlarmScreen(navController: NavController) {
    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    val alarms = remember { mutableStateListOf<Pair<Int, Int>>() }

    val context = LocalContext.current

    AlarmClockTheme {
        Scaffold(bottomBar = { NavigationBarComponent(navController) }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (showTimePicker) {
                        TimePickerDialog(
                            initialHour = hour,
                            initialMinute = minute,
                            onTimePicked = { h, m ->
                                hour = h
                                minute = m
                                alarms.add(Pair(h, m))
                                showTimePicker = false
                                setAlarm(context, h, m)
                            },
                            onDismiss = { showTimePicker = false }
                        )
                    }

                    AlarmTimeSelector(hour, minute, onPickTime = { showTimePicker = true })
                    Spacer(modifier = Modifier.height(16.dp))

                    AlarmList(alarms)
                }
            }
        }
    }
}

@Composable
fun AlarmTimeSelector(hour: Int, minute: Int, onPickTime: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selected Alarm Time", fontSize = 18.sp)
            Text(
                text = String.format("%02d:%02d", hour, minute),
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onPickTime) {
                Text("Pick a Time")
            }
        }
    }
}

@Composable
fun AlarmList(alarms: List<Pair<Int, Int>>) {
    if (alarms.isNotEmpty()) {
        LazyColumn {
            items(alarms.size) { index ->
                AlarmItem(alarms[index].first, alarms[index].second)
            }
        }
    } else {
        Text("No alarms set.", fontSize = 16.sp)
    }
}

@Composable
fun AlarmItem(hour: Int, minute: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = String.format("%02d:%02d", hour, minute))
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimePicked: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    android.app.TimePickerDialog(
        context,
        { _, h, m ->
            onTimePicked(h, m)
        },
        initialHour,
        initialMinute,
        true
    ).show()
}

fun setAlarm(context: Context, hour: Int, minute: Int) {
    Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
}
