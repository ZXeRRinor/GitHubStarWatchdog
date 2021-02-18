package com.zxerrinor.githubstarwatchdog.views

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface FindRepoView : MvpView {
    @AddToEndSingle
    fun setRepoInputAdapter(repoList: List<String>)

    @AddToEndSingle
    fun setHideNotLoadedSwitchVisibility(visible: Boolean)
}