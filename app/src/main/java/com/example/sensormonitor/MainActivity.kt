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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var selectedSensors = mutableListOf<Sensor>()
    private var sensorData by mutableStateOf("센서 데이터가 여기에 표시됩니다.")
    private var samplingRate by mutableIntStateOf(1000)
    private val dataPoints = mutableStateListOf<Entry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setContent {
            SensorDataApp()
        }
    }

    @Composable
    fun SensorDataApp() {
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
                        SensorManager.SENSOR_DELAY_UI
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
            Button(onClick = {
                // CSV 저장 버튼
                val selectedSensor = sensorList.getOrNull(selectedSensorIndex)
                if (selectedSensor != null) {
                    // 센서 이름과 데이터를 CSV로 저장
                    val sensorValues = selectedSensor.name
                    SensorUtils.saveSensorDataToCsv(this@MainActivity, "sensor_data.csv", System.currentTimeMillis(), floatArrayOf(0.0f))
                }
            }) {
                Text("CSV로 저장")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(sensorData, style = MaterialTheme.typography.bodyLarge)
        }
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val timestamp = System.currentTimeMillis()
            val sensorValues = event.values // event.values는 FloatArray로 반환됩니다.
            val data = "센서: ${event.sensor.name} | 값: ${sensorValues.joinToString(", ")} | 시간: $timestamp\n"
            sensorData = data

            // 센서 값을 CSV 파일로 저장하는 부분
            SensorUtils.saveSensorDataToCsv(this, "sensor_data.csv", timestamp, sensorValues)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
