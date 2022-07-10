package com.dayo.jetpack.covidtrackerapp

import android.graphics.RectF
import android.service.chooser.ChooserTargetService
import android.util.Log
import com.dayo.jetpack.covidtrackerapp.services.CovidData
import com.robinhood.spark.SparkAdapter
import java.sql.Time

class CovidSparkAdapter(private val dailyData: List<CovidData>) : SparkAdapter() {

    var metric = Metric.POSTIVE
    var daysAgo = TimeScale.MAX

    override fun getCount() = dailyData.size

    override fun getItem(index: Int) = dailyData[index]

    override fun getY(index: Int): Float {
        val chosenDayData = dailyData[index]
        return when(metric){
            Metric.POSTIVE -> chosenDayData.positiveIncrease!!.toFloat()
            Metric.NEGATIVE -> chosenDayData.negativeIncrease!!.toFloat()
            Metric.DEATH -> chosenDayData.deathIncrease!!.toFloat()
        }
    }

    override fun getDataBounds(): RectF {
        val bounds = super.getDataBounds()
        Log.i("Result", "Here to log the days ago that is passed ${daysAgo.numDays}")
        Log.i("Result", "Here to log the count $count")
        if(daysAgo != TimeScale.MAX){
            apply {
                bounds.left = count - daysAgo.numDays.toFloat()
            }
        }
        return bounds
    }

}
