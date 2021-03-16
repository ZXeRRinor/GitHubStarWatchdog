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
        viewState.setRepoInputAdapter(getRepositoriesOfUser(repoUserName, true))
        viewState.setRepoInputAdapter(getRepositoriesOfUser(repoUserName))
    }

    private fun getRepositoriesOfUser(repoUserName: String, forceUseDb: Boolean = false): List<String> {
        if (CurrentValuesStore.repositoriesOfUser != null && CurrentValuesStore.repoUserName == repoUserName)
            return CurrentValuesStore.repositoriesOfUser!!
        CurrentValuesStore.repoUserName = repoUserName
        val result = (if (isInternetAvailable() && !offlineMode && !forceUseDb)
            loadRepositoriesOfUserFromGitHub(repoUserName)
        else
            loadRepositoriesOfUserFromDb(repoUserName)).sorted()
        CurrentValuesStore.repositoriesOfUser = result
        return result
    }

    private fun loadRepositoriesOfUserFromGitHub(repoUserName: String): List<String> {
        val repositories = mutableListOf<Repository>()
        var page = 0
        do {
            page++
            val currentRepos =
                App.gitHubApi.getRepositoriesOfUser(
                    repoUserName,
                    page,
                    100
                ).execute().body()
                    ?: throw IllegalArgumentException("No repositories for this user found")
            repositories.addAll(currentRepos)
        } while (currentRepos.size == 100)
        saveRepositoriesToDb(repositories)
        return repositories.map { it.name }
    }

    private fun loadRepositoriesOfUserFromDb(repoUserName: String): List<String> {
        return if (hideNotLoaded)
            App.db.repositoryDao().findAllByRepoUserName(repoUserName).mapNotNull {
                if (App.db.starDao()
                        .countByRepoNameAndRepoUserName(
                            it.repoName,
                            it.repoUserName
                        ) > 0
                ) it.repoName else null
            }
        else
            App.db.repositoryDao().findAllByRepoUserName(repoUserName)
                .map { it.repoName }
    }

    private fun saveRepositoriesToDb(repositories: List<Repository>) = GlobalScope.launch {
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
}