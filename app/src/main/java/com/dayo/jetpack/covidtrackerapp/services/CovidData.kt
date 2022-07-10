package com.dayo.jetpack.covidtrackerapp.services

import com.google.gson.annotations.SerializedName
import java.util.*

data class CovidData(
    @SerializedName("dateChecked")
    var dateChecked : Date,

    @SerializedName("positiveIncrease")
    var positiveIncrease : Int? = null,

    @SerializedName("negativeIncrease")
    var negativeIncrease : Int? = null,

    @SerializedName("deathIncrease")
    var deathIncrease : Int? = null,

    @SerializedName("state")
    var state : String? = null
)
