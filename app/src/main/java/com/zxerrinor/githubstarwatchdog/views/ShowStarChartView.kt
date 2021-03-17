package com.zxerrinor.githubstarwatchdog.views

import com.github.mikephil.charting.data.BarEntry
import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType

interface ShowStarChartView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setChartEntries(entries: List<BarEntry>, label: String, start: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIsFavouriteSwitchState(state: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setMonthInputAdapter(monthList: List<String>)
}