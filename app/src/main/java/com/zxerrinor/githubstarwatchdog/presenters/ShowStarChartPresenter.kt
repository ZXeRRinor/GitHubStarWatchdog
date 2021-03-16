package com.zxerrinor.githubstarwatchdog.presenters

import android.widget.Toast
import com.github.mikephil.charting.data.BarEntry
import com.zxerrinor.githubstarwatchdog.App
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.database.Repository
import com.zxerrinor.githubstarwatchdog.database.Star
import com.zxerrinor.githubstarwatchdog.githubapi.Stargazer
import com.zxerrinor.githubstarwatchdog.isInternetAvailable
import com.zxerrinor.githubstarwatchdog.views.ShowStarChartView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpPresenter
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.math.ceil

class ShowStarChartPresenter : MvpPresenter<ShowStarChartView>() {
    private var monthList = mapOf<String, Int>()

    fun onCurrentRepoFavouriteSwitchClicked() = GlobalScope.launch {
        val repo = App.db.repositoryDao().findByRepoNameAndRepoUserName(
            CurrentValuesStore.repoName,
            CurrentValuesStore.repoUserName
        )!!
        val currentIsFavouriteValue = repo.isFavourite
        App.db.repositoryDao().delete(repo)
        App.db.repositoryDao()
            .insertAll(
                Repository(
                    repo.repoId,
                    repo.repoUserName,
                    repo.repoName,
                    repo.stars,
                    !currentIsFavouriteValue,
                    repo.createdAt
                )
            )
    }

    override fun onFirstViewAttach() {
        GlobalScope.launch {
            val repoUserName = CurrentValuesStore.repoUserName
            val repoName = CurrentValuesStore.repoName
            val currentTime = LocalDateTime.now()
            val startDate =
                currentTime.minusMonths(11).minusDays(currentTime.dayOfMonth.toLong() - 1)
            val xAxisStart = startDate.monthValue
            var usersOfMonths = loadFromDb(repoName, repoUserName, startDate)
            show(usersOfMonths, repoName, repoUserName, xAxisStart)
            if (isInternetAvailable() && !CurrentValuesStore.offlineMode) {
                usersOfMonths = loadFromGitHubAndSaveToDb(repoUserName, repoName, startDate)
                show(usersOfMonths, repoName, repoUserName, xAxisStart)
                showUpdateStatusToast(true)
            } else showUpdateStatusToast(false)
        }
    }

    private fun showUpdateStatusToast(updated: Boolean) {
        val toastText = if (updated) "Chart data updated!" else "Offline mode"
        CurrentValuesStore.activity.runOnUiThread {
            Toast.makeText(
                CurrentValuesStore.activity, toastText,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadFromGitHubAndSaveToDb(
        repoUserName: String,
        repoName: String,
        startDate: LocalDateTime
    ): Map<Int, MutableList<String>> {
        val repository =
            App.gitHubApi.getRepositoryInfo(repoUserName, repoName).execute().body()
                ?: throw IllegalArgumentException("Not found repository with such name")
        val dbRepo = App.db.repositoryDao().findById(repository.id)
        if (dbRepo != null) viewState.setIsFavouriteSwitchState(dbRepo.isFavourite)
        var page = ceil(repository.stargazers_count / 100.0).toInt()
        val usersOfMonths = mutableMapOf<Int, MutableList<String>>()
        var breakNeeded = false
        while (page >= 0 && !breakNeeded) {
            val response =
                App.gitHubApi
                    .getStargazers(repoUserName, repoName, page, 100).execute()
            page--
            (response.body()
                ?: throw IllegalArgumentException("not found stargazers for this repository")).forEach {
                saveToDb(repoUserName, repoName, it)
                val date = LocalDateTime.parse(it.starred_at.replace("Z", ""))
                if (date >= startDate) {
                    val key =
                        if (date.year == LocalDateTime.now().year) date.monthValue + 12 else date.monthValue
                    if (usersOfMonths[key] != null) usersOfMonths[key]!!.add(
                        it.user.login
                    )
                    else usersOfMonths[key] = mutableListOf(it.user.login)
                } else breakNeeded = true
            }
        }
        return usersOfMonths
    }

    private fun saveToDb(repoUserName: String, repoName: String, stargazer: Stargazer) {
        GlobalScope.launch {
            if (App.db.starDao().findByStargazerUserNameAndStarredAt(
                    stargazer.user.login,
                    stargazer.starred_at
                ) == null
            )
                App.db.starDao().insertAll(
                    Star(
                        repoUserName,
                        repoName,
                        stargazer.user.login,
                        stargazer.starred_at
                    )
                )
        }
    }

    private fun loadFromDb(
        repoName: String,
        repoUserName: String,
        startDate: LocalDateTime
    ): Map<Int, MutableList<String>> {
        val usersOfMonths = mutableMapOf<Int, MutableList<String>>()
        val currentTime = LocalDateTime.now()
        GlobalScope.launch {
            val dbRepo =
                App.db.repositoryDao().findByRepoNameAndRepoUserName(repoName, repoUserName)
            if (dbRepo != null) viewState.setIsFavouriteSwitchState(dbRepo.isFavourite)
        }
        App.db.starDao().findAllByRepoNameAndRepoUserName(repoName, repoUserName)
            .forEach {
                val date = LocalDateTime.parse(it.starredAt.replace("Z", ""))
                if (date >= startDate) {
                    val key =
                        if (date.year == currentTime.year) date.monthValue + 12 else date.monthValue
                    if (usersOfMonths[key] != null) usersOfMonths[key]!!.add(
                        it.stargazerUsername
                    )
                    else usersOfMonths[key] = mutableListOf(it.stargazerUsername)
                }
            }
        return usersOfMonths
    }

    private fun show(
        usersOfMonths: Map<Int, MutableList<String>>,
        repoName: String,
        repoUserName: String,
        xAxisStart: Int
    ) {
        val currentTime = LocalDateTime.now()
        CurrentValuesStore.months = usersOfMonths
        viewState.setChartEntries(
            usersOfMonths.map { BarEntry(it.key.toFloat(), it.value.size.toFloat()) },
            "$repoUserName/$repoName",
            xAxisStart
        )
        monthList = usersOfMonths.map {
            (Month.values()[(it.key - 1) % 12].name.toLowerCase(Locale.ROOT)
                .capitalize(Locale.ROOT) +
                    if (it.key - 12 > 0) " (${currentTime.year})"
                    else " (${currentTime.year - 1})") to it.key
        }.toMap()
        viewState.setMonthInputAdapter(monthList.map { it.key })
    }

    fun onShowMonthButtonClicked(monthInput: String) {
        CurrentValuesStore.month = monthList[monthInput]!!
    }
}