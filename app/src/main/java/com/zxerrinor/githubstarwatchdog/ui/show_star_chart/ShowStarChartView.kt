package com.zxerrinor.githubstarwatchdog.ui.show_star_chart

import com.github.mikephil.charting.data.BarEntry
import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.zxerrinor.githubstarwatchdog.ui.base.BaseView

interface ShowStarChartView : BaseView {
    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setChartEntries(entries: List<BarEntry>, label: String, start: Int)

    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setIsFavouriteSwitchState(state: Boolean)

    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setMonthSpinnerContent(monthList: List<String>)
}