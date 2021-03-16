package com.zxerrinor.githubstarwatchdog.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle

interface FindRepoView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setRepoInputAdapter(repoList: List<String>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setHideNotLoadedSwitchVisibility(visible: Boolean)
}