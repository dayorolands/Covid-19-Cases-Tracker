package com.dayo.jetpack.covidtrackerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.dayo.jetpack.covidtrackerapp.databinding.ActivityMainBinding
import com.dayo.jetpack.covidtrackerapp.services.CovidData
import com.dayo.jetpack.covidtrackerapp.services.RetrofitService
import com.robinhood.ticker.TickerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"
private const val ALL_STATES = "All (Nationwide)"
class MainActivity : AppCompatActivity() {
    private lateinit var currentDisplayData: List<CovidData>
    private lateinit var covidSparkAdapter: CovidSparkAdapter
    private lateinit var nationalDailyData: List<CovidData>
    private lateinit var perStateDailyData: Map<String, List<CovidData>>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.app_description)

        //Fetch the national data
        RetrofitService.retrofit.getNationalData().enqueue(object: Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.w(TAG, "onResponse $response")
                val nationalData = response.body()!!
                if(nationalData == null){
                    Log.e(TAG, "Did not receive a valid response body")
                    return
                }
                setupEventListeners()
                nationalDailyData = nationalData.reversed()
                updateDisplayWithData(nationalDailyData)
            }
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }
        })

        //Fetch the state data
        RetrofitService.retrofit.getStateData().enqueue(object : Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.w(TAG, "onResponse $response")
                val stateData = response.body()
                if(stateData == null){
                    Log.e(TAG, "Did not receive a valid response body")
                    return
                }
                perStateDailyData = stateData.reversed().groupBy{ it.state!! }
                Log.i(TAG, "Update the spinner with the state data")
                updateSpinnerWithStateData(perStateDailyData.keys)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

        })
    }

    private fun updateSpinnerWithStateData(stateNames: Set<String>) {
        val stateAbbreviationList = stateNames.toMutableList()
        stateAbbreviationList.sort()
        stateAbbreviationList.add(0, ALL_STATES)

        binding.spinnerSelect.attachDataSource(stateAbbreviationList)
        binding.spinnerSelect.setOnSpinnerItemSelectedListener { parent, _, position, _ ->
            val selectedState = parent.getItemAtPosition(position)
            val selectedData = perStateDailyData[selectedState] ?: nationalDailyData
            updateDisplayWithData(selectedData)
        }
    }

    private fun setupEventListeners() {
        binding.tvMetricLabel.setCharacterLists(TickerUtils.provideNumberList())
        binding.sparkView.isScrubEnabled = true
        binding.sparkView.setScrubListener { itemData ->
            if(itemData is CovidData){
                updateInforForDate(itemData)
            }
        }

        binding.radioGroupTimeSelection.setOnCheckedChangeListener { _, checkedId ->
            covidSparkAdapter.daysAgo = when(checkedId){
                R.id.radioButtonWeek -> TimeScale.WEEK
                R.id.radioButtonMonth -> TimeScale.MONTH
                else -> TimeScale.MAX
            }
            covidSparkAdapter.notifyDataSetChanged()
        }

        binding.radioGroupMetricSelection.setOnCheckedChangeListener { _, checkedId ->
           when(checkedId){
                R.id.radioButtonPositive -> updateDisplayMetric(Metric.POSTIVE)
                R.id.radioButtonNegative -> updateDisplayMetric(Metric.NEGATIVE)
                R.id.radioButtonDeath -> updateDisplayMetric(Metric.DEATH)
            }
        }
    }

    private fun updateDisplayMetric(metric: Metric) {
        val colorRes = when(metric){
            Metric.POSTIVE -> R.color.colorPositive
            Metric.NEGATIVE -> R.color.colorNegative
            Metric.DEATH -> R.color.colorDeath
        }

        @ColorInt val colorInt = ContextCompat.getColor(this, colorRes)
        binding.sparkView.lineColor = colorInt
        binding.tvMetricLabel.setTextColor(colorInt)
        covidSparkAdapter.metric = metric
        covidSparkAdapter.notifyDataSetChanged()

        updateInforForDate(currentDisplayData.last())
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>){
        //Create a new SparkAdpapter with the data
        //Update radio buttons to select the positive case and max time by default
        //Display the metric for the most recent date
        //method below updates the two text views in our main activity xml
        currentDisplayData = dailyData
        covidSparkAdapter = CovidSparkAdapter(dailyData)
        binding.sparkView.adapter = covidSparkAdapter
        binding.radioButtonPositive.isChecked = true
        binding.radioButtonMax.isChecked = true
        updateDisplayMetric(Metric.POSTIVE)
    }

    private fun updateInforForDate(covidData: CovidData){
        val numberCases = when(covidSparkAdapter.metric){
            Metric.DEATH -> covidData.deathIncrease
            Metric.NEGATIVE -> covidData.negativeIncrease
            Metric.POSTIVE -> covidData.positiveIncrease
        }
        binding.tvMetricLabel.text = NumberFormat.getInstance().format(numberCases)
        val outputDateChecked = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        binding.tvDateLabel.text = outputDateChecked.format(covidData.dateChecked)
    }
}