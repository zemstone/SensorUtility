package com.example.sensormonitor

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.io.FileWriter
import java.io.IOException

object SensorUtils {

    // 📌 CSV 파일에 센서 데이터 저장
    fun saveSensorDataToCsv(context: Context, fileName: String, timestamp: Long, values: FloatArray) {
        val file = File(context.filesDir, fileName)
        try {
            val writer = FileWriter(file, true)
            val dataLine = "$timestamp,${values.joinToString(",")}\n"
            writer.append(dataLine)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 📌 MPAndroidChart LineChart 설정
    fun setupChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
        }
    }

    // 📌 CSV 데이터를 그래프로 변환하여 LineChart에 표시
    fun loadCsvAndPlotGraph(context: Context, chart: LineChart, fileName: String) {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return

            val entries = mutableListOf<Entry>()
            file.forEachLine { line ->
                val data = line.split(",")
                if (data.size >= 2) {
                    val x = data[0].toFloatOrNull() ?: return@forEachLine
                    val y = data[1].toFloatOrNull() ?: return@forEachLine
                    entries.add(Entry(x, y))
                }
            }

            val dataSet = LineDataSet(entries, "Sensor Data").apply {
                color = android.graphics.Color.BLUE
                valueTextSize = 10f
            }
            val lineData = LineData(dataSet)

            chart.post {
                chart.data = lineData
                chart.invalidate()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
