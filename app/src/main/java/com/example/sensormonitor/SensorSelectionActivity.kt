package com.example.sensormonitor

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SensorSelectionActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListView: ListView
    private lateinit var minDelayTextView: TextView
    private lateinit var confirmButton: Button

    private var selectedSensors = mutableListOf<Sensor>()
    private var sensorMap = mutableMapOf<Sensor, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_selection)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorListView = findViewById(R.id.sensorListView)
        minDelayTextView = findViewById(R.id.minDelayTextView)
        confirmButton = findViewById(R.id.confirmButton)

        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, sensorList.map { it.name })

        sensorListView.adapter = adapter
        sensorListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        sensorListView.setOnItemClickListener { _, _, position, _ ->
            val sensor = sensorList[position]
            if (selectedSensors.contains(sensor)) {
                selectedSensors.remove(sensor)
            } else {
                selectedSensors.add(sensor)
            }
            updateMinDelay()
        }

        confirmButton.setOnClickListener {
            val resultsIntent = intent
            resultsIntent.putExtra("selectedSensors", selectedSensors.map { it.type }.toIntArray())
            setResult(Activity.RESULT_OK, resultsIntent)
            finish()
        }
    }

    private fun updateMinDelay() {
        val minDelay = selectedSensors.maxOfOrNull { it.minDelay } ?: 0
        val minDelayMs = if (minDelay > 0) minDelay / 1000 else "N/A"
        minDelayTextView.text = "최소 주기: $minDelayMs ms"
    }
}