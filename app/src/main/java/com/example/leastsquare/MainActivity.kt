package com.example.leastsquare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries


class MainActivity : AppCompatActivity() {
    private val valuePairList = mutableListOf<Pair<Double, Double>>()

    private var maxX = -9999999.0
    private var minX = 9999999.0
    private var maxY = -9999999.0
    private var minY = 9999999.0
    private var meanX = 0.0
    private var meanY = 0.0

    private var pointCount = 0
    private var sumX = 0.0
    private var sumY = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.cfButton).setOnClickListener{
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            readCsv(data?.data) //The uri with the location of the file
        }
    }

    private fun readCsv(uri: Uri?){
        if (uri?.path == null) return
        contentResolver.openInputStream(uri)?.let{
            parseData(it.reader().readText())
        }
    }

    private fun parseData(rawCsv: String){
        val lines = rawCsv.split("\n")
        val clearedLines = mutableListOf<String>()
        lines.forEach{
            clearedLines.add(it.replace(" ", ""))
        }
        clearedLines.forEach { it ->
            try {
                it.split(",").let { numbers ->
                    val coord = numbers[0].toDoubleOrNull()
                    val value = numbers[1].toDoubleOrNull()
                    if (coord != null && value != null) {
                        valuePairList.add(Pair(coord, value))
                        if (coord >= maxX) maxX = coord
                        else if (coord <= minX) minX = coord
                        if (value >= maxY) maxY = value
                        else if (value <= minY) minY = value
                    }
                }
            } catch (e: Exception) { Log.e("asd", "$e")}
        }
        valuePairList.sortBy{ it.first }
        drawPoints()
        updateButton()
    }

    private fun drawPoints(){
        val dataPointList = mutableListOf<DataPoint>()
        valuePairList.forEach {
            dataPointList.add(DataPoint(it.first, it.second))
        }
        findViewById<GraphView>(R.id.graph).addSeries(
            PointsGraphSeries<DataPoint>(dataPointList.toTypedArray())
        )
    }

    private fun updateButton(){
        findViewById<Button>(R.id.cfButton).apply{
            text = "Calculate"
            setOnClickListener {
                leastSquare()
            }
        }
    }

    private fun leastSquare(): String{
        val slope = calcSlope()
        val yInterception = meanY - (slope*meanX)
        drawFunction(slope, yInterception)
        return "y = $slope x + $yInterception".also{
            findViewById<TextView>(R.id.solutionTextView).text = it
        }
    }

    private fun drawFunction(slope: Double, yInterception: Double){
        val dataPointList = mutableListOf<DataPoint>()
        for (i in 0..100){
            dataPointList.add(DataPoint(i.toDouble(), slope*i+yInterception))
        }
        findViewById<GraphView>(R.id.graph).addSeries(
            LineGraphSeries<DataPoint>(dataPointList.toTypedArray())
        )
    }

    private fun calcSlope(): Double{
        pointCount = valuePairList.lastIndex + 1
        sumX = 0.0
        sumY = 0.0
        valuePairList.forEach{
            sumX += it.first
            sumY += it.second
        }
        var numer  = 0.0
        var denom  = 0.0
        meanX = sumX/pointCount
        meanY = sumY/pointCount
        valuePairList.forEach{
            numer += ((it.first-meanX) * (it.second-meanY))
            denom += ((it.first-meanX) * (it.first-meanX))
        }
        return numer / denom
    }
}