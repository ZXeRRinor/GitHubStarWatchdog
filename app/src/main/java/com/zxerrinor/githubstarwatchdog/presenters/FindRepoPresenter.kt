package com.zxerrinor.githubstarwatchdog.presenters

import com.zxerrinor.githubstarwatchdog.App
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore.offlineMode
import com.zxerrinor.githubstarwatchdog.githubapi.Repository
import com.zxerrinor.githubstarwatchdog.isInternetAvailable
import com.zxerrinor.githubstarwatchdog.views.FindRepoView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpPresenter
import com.zxerrinor.githubstarwatchdog.database.Repository as RepositoryRecord

class FindRepoPresenter : MvpPresenter<FindRepoView>() {
    var hideNotLoaded: Boolean = false
        set(value) {
            field = value
            CurrentValuesStore.repositoriesOfUser = null
        }

    override fun onFirstViewAttach() {
//        viewState.showMessage("kek");
    }

    fun onLoadButtonClicked(repoName: String) {
        CurrentValuesStore.repoName = repoName
    }

    fun onOfflineModeSwitchClicked() {
        offlineMode = !offlineMode
        viewState.setHideNotLoadedSwitchVisibility(offlineMode)
    }

    fun onFindRepoButtonClicked(repoUserName: String) = GlobalScope.launch {
        viewState.setRepoInputAdapter((if (CurrentValuesStore.repositoriesOfUser != null && CurrentValuesStore.repoUserName == repoUserName) CurrentValuesStore.repositoriesOfUser!!
        else {
            CurrentValuesStore.repoUserName = repoUserName
            val result = if (isInternetAvailable() && !offlineMode) {
                val repositories = mutableListOf<Repository>()
                var page = 0
                do {
                    val gitHubUserRepositoriesResponse =
                        App.gitHubApi.getRepositoriesOfUser(
                            repoUserName,
                            page,
                            100
                        )
                            .execute()
                    page++
                    val currentRepos = gitHubUserRepositoriesResponse.body() ?: return@launch
                    repositories.addAll(currentRepos)
                } while (currentRepos.size == 100)
                GlobalScope.launch {
                    repositories.forEach {
                        if (App.db.repositoryDao().findById(it.id) == null) App.db.repositoryDao()
                            .insertAll(
                                RepositoryRecord(
                                    it.id,
                                    it.owner.login,
                                    it.name,
                                    it.stargazers_count,
                                    false,
                                    it.created_at
                                )
                            )
                    }
                }
                repositories.map { it.name }
            } else if (hideNotLoaded)
                App.db.repositoryDao().findAllByRepoUserName(repoUserName).mapNotNull {
                    if (App.db.starDao()
                            .countByRepoNameAndRepoUserName(it.repoName, it.repoUserName) > 0
                    ) it.repoName else null
                }
            else
                App.db.repositoryDao().findAllByRepoUserName(repoUserName).map { it.repoName }
            CurrentValuesStore.repositoriesOfUser = result
            result
        }))
    }
}