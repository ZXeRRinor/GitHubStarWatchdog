package com.zxerrinor.githubstarwatchdog.ui.base.show_star_chart

import com.github.mikephil.charting.data.BarEntry
import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType

interface ShowStarChartView : OmegaView {
    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setChartEntries(entries: List<BarEntry>, label: String, start: Int)

    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setIsFavouriteSwitchState(state: Boolean)

    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setMonthSpinnerContent(monthList: List<String>)
}