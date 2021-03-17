package com.zxerrinor.githubstarwatchdog.views

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType

interface FindRepoView : MvpView {
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setRepoInputAdapter(repoList: List<String>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setHideNotLoadedSwitchVisibility(visible: Boolean)
}