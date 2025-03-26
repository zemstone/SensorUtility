package com.example.sensormonitor

import android.content.Context
import java.io.File

object SensorUtils {
    // 센서 데이터를 CSV 파일로 저장하는 함수
    fun saveSensorDataToCsv(context: Context, fileName: String, timestamp: Long, sensorValues: FloatArray) {
        val file = File(context.filesDir, fileName)

        // 파일이 존재하지 않으면 헤더 작성
        if (!file.exists()) {
            file.createNewFile()
            file.appendText("Timestamp, Sensor Values\n")
        }

        // sensorValues를 문자열로 변환하여 CSV에 저장
        val sensorValuesString = sensorValues.joinToString(", ") { it.toString() } // FloatArray를 콤마로 구분된 문자열로 변환
        file.appendText("$timestamp, $sensorValuesString\n")
    }
}
