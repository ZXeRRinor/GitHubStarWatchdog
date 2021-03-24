package com.zxerrinor.githubstarwatchdog.views

import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType

interface FindRepoView : OmegaView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setRepoInputAdapter(repoList: List<String>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setHideNotLoadedSwitchVisibility(visible: Boolean)
}