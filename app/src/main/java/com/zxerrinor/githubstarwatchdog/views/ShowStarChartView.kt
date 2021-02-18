package com.zxerrinor.githubstarwatchdog.views

import com.github.mikephil.charting.data.BarEntry
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface ShowStarChartView : MvpView {
    @AddToEndSingle
    fun setChartEntries(entries: List<BarEntry>, label: String, start: Int)

    @AddToEndSingle
    fun setIsFavouriteSwitchState(state: Boolean)

    @AddToEndSingle
    fun setMonthInputAdapter(monthList: List<String>)
}