package com.example.leastsquare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val valuePairList = mutableListOf<Pair<Double, Double>>()

    private var maxX = -9999999.0
    private var minX = 9999999.0
    private var maxY = -9999999.0
    private var minY = 9999999.0

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
            it.split(",").let{ numbers ->
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
        }
        //drawPoints()
        updateButton()
    }

    private fun drawPoints(){
        val height = 500*resources.displayMetrics.density
        val width = resources.displayMetrics.widthPixels
        val oneStepSizeX = width / kotlin.math.abs(minX - maxX)
        val oneStepSizeY = height / kotlin.math.abs(minY - maxY)
        //TODO draw the coordinate system and position the iws properly, commented out in the mean while
        valuePairList.forEach{
            findViewById<LinearLayout>(R.id.coordSystem).addView(
                    ImageView(this.applicationContext).apply {
                        background = resources.getDrawable(R.drawable.ic_baseline_brightness_1_24)
                    }
            )
        }
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
        val yInterception = (sumY-slope*sumX)/pointCount
        return "y = $slope x + $yInterception".also{
            findViewById<TextView>(R.id.solutionTextView).text = it
        }
    }

    private fun calcSlope(): Double{
        pointCount = valuePairList.lastIndex + 1
        sumX = 0.0
        sumY = 0.0
        var sumXY = 0.0
        var sumXSquare = 0.0
        valuePairList.forEach{
            sumX += it.first
            sumY += it.second
            sumXY += it.second * it.first
            sumXSquare += it.first * it.first
        }
        val a = pointCount*sumXY-sumY*sumY
        val b = pointCount*sumXSquare-sumX*sumX
        return a/b
    }
}