package com.zxerrinor.githubstarwatchdog.ui.find_repo

import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.zxerrinor.githubstarwatchdog.ui.base.BaseView

interface FindRepoView : BaseView {
    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setRepoSpinnerContent(repoList: List<String>)

    @StateStrategyType(StrategyType.ADD_TO_END_SINGLE)
    fun setHideNotLoadedSwitchVisibility(visible: Boolean)
}