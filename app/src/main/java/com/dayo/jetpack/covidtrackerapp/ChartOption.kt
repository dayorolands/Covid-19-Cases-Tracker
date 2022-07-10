package com.dayo.jetpack.covidtrackerapp

enum class Metric{
    NEGATIVE, POSTIVE, DEATH
}

enum class TimeScale(val numDays: Int){
    WEEK(7),
    MONTH(30),
    MAX(-1);

    companion object{
        fun find (index: Int): TimeScale{
            return values().find { it.numDays == index }
                    ?: throw IndexOutOfBoundsException("No time scale exist with those $index days")
        }
    }
}