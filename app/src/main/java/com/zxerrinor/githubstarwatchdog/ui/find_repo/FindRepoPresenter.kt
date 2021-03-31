package com.zxerrinor.githubstarwatchdog.ui.find_repo

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.libs.omegatypes.toText
import com.zxerrinor.githubstarwatchdog.*
import com.zxerrinor.githubstarwatchdog.githubapi.Repository
import com.zxerrinor.githubstarwatchdog.ui.base.BasePresenter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.zxerrinor.githubstarwatchdog.database.Repository as RepositoryRecord

class FindRepoPresenter : BasePresenter<FindRepoView>() {

    private var repoUserName = ""
    private var offlineMode = false

    private val fragment: FindRepoFragment
        get() {
            val fragment = this.attachedViews.toList().first()
            if (fragment is FindRepoFragment) return fragment
            else throw IllegalStateException("Illegal object")
        }

    var hideNotLoaded: Boolean = false

    override fun onFirstViewAttach() {
        // nothing
    }

    fun onLoadButtonClicked(repoName: String?) {
        if (repoName.isNullOrEmpty()) {
          viewState.showToast("Please, select repository for watching".toText())
        } else {
//        CurrentValuesStore.repoName = repoName
            val bundle = Bundle()
            bundle.putString(REPO_NAME_ARGUMENT_NAME, repoName)
            bundle.putString(REPO_USER_NAME_ARGUMENT_NAME, repoUserName)
            bundle.putBoolean(OFFLINE_MODE_ARGUMENT_NAME, offlineMode)
            findNavController(fragment).navigate(
                R.id.action_FindRepoFragment_to_ShowChartFragment,
                bundle
            )
        }
    }

    fun onOfflineModeSwitchClicked() {
        offlineMode = !offlineMode
        viewState.setHideNotLoadedSwitchVisibility(offlineMode)
    }

    fun onFindRepoButtonClicked(repoUserName: String) {
        GlobalScope.launch {
            viewState.setRepoSpinnerContent(getRepositoriesOfUser(repoUserName, true))
            viewState.setRepoSpinnerContent(getRepositoriesOfUser(repoUserName))
        }
    }

    private fun getRepositoriesOfUser(
        repoUserName: String,
        forceUseDb: Boolean = false
    ): List<String> {
        this.repoUserName = repoUserName
        return (if (isInternetAvailable() && !offlineMode && !forceUseDb)
            loadRepositoriesOfUserFromGitHub(repoUserName)
        else
            loadRepositoriesOfUserFromDb(repoUserName)).sorted()
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