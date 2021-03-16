package com.zxerrinor.githubstarwatchdog.views

import com.github.mikephil.charting.data.BarEntry
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle

interface ShowStarChartView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setChartEntries(entries: List<BarEntry>, label: String, start: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIsFavouriteSwitchState(state: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setMonthInputAdapter(monthList: List<String>)
}