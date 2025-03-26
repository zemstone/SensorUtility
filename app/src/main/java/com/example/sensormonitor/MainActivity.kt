package com.example.sensormonitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var selectedSensors = mutableListOf<Sensor>()
    private var sensorData by mutableStateOf("센서 데이터가 여기에 표시됩니다.")
    private var samplingRate by mutableIntStateOf(1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            SensorDataApp()
        }
    }

    @Composable
    fun SensorDataApp() {
        val context = LocalContext.current
        var intervalText by remember { mutableStateOf("1000") }
        var selectedSensorIndex by remember { mutableIntStateOf(0) }
        var expanded by remember { mutableStateOf(false) }
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("센서 데이터 수집기", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { expanded = true }) {
                Text(text = sensorList.getOrNull(selectedSensorIndex)?.name ?: "센서 선택")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sensorList.forEachIndexed { index, sensor ->
                    DropdownMenuItem(
                        text = { Text(sensor.name) },
                        onClick = {
                            selectedSensorIndex = index
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = intervalText,
                onValueChange = { intervalText = it },
                label = { Text("수집 주기 (ms)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = samplingRate.toFloat(),
                onValueChange = { samplingRate = it.toInt() },
                valueRange = 100f..5000f
            )
            Text("현재 주기: ${samplingRate} ms")

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                samplingRate = intervalText.toIntOrNull() ?: 1000
                val selectedSensor = sensorList.getOrNull(selectedSensorIndex)
                if (selectedSensor != null) {
                    selectedSensors.clear()
                    selectedSensors.add(selectedSensor)
                    sensorManager.registerListener(
                        this@MainActivity,
                        selectedSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                    sensorData = "데이터 수집 시작"
                }
            }) {
                Text("시작")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                stopSensors()
                sensorData = "데이터 수집 중지"
            }) {
                Text("정지")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(sensorData, style = MaterialTheme.typography.bodyLarge)

            // 📌 AndroidView를 사용하여 LineChart 추가
            Spacer(modifier = Modifier.height(16.dp))
            AndroidView(
                factory = { ctx -> LineChart(ctx).apply { SensorUtils.setupChart(this) } },
                modifier = Modifier.fillMaxWidth().height(300.dp)
            ) { chart ->
                SensorUtils.loadCsvAndPlotGraph(context, chart, "sensor_data.csv")
            }
        }
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val timestamp = System.currentTimeMillis()
            val sensorValues = event.values.joinToString(", ")
            val data = "센서: ${event.sensor.name} | 값: $sensorValues | 시간: $timestamp\n"
            sensorData = data

            // 📌 센서 데이터 CSV 파일에 저장
            SensorUtils.saveSensorDataToCsv(this, "sensor_data.csv", timestamp, event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
