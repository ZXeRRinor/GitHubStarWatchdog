package com.zxerrinor.githubstarwatchdog.presenters

import android.widget.Toast
import com.github.mikephil.charting.data.BarEntry
import com.zxerrinor.githubstarwatchdog.App
import com.zxerrinor.githubstarwatchdog.CurrentValuesStore
import com.zxerrinor.githubstarwatchdog.database.Repository
import com.zxerrinor.githubstarwatchdog.database.Star
import com.zxerrinor.githubstarwatchdog.isInternetAvailable
import com.zxerrinor.githubstarwatchdog.views.ShowStarChartView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpPresenter
import java.time.LocalDateTime
import java.time.Month
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
            val months = mutableMapOf<Int, Int>()
            val currentTime = LocalDateTime.now()
            val startDate =
                currentTime.minusMonths(11).minusDays(currentTime.dayOfMonth.toLong() - 1)
            val xAxisStart = startDate.monthValue
            val usersOfMonths = loadFromDB(repoName, repoUserName, startDate)
            showAndSave(usersOfMonths, repoName, repoUserName, xAxisStart)
            var breakNeeded = false
            if (isInternetAvailable() && !CurrentValuesStore.offlineMode) {
                val repo =
                    App.gitHubApi.getRepositoryInfo(repoUserName, repoName).execute()
                val repository = repo.body() ?: return@launch
                val dbRepo = App.db.repositoryDao().findById(repository.id)
                if (dbRepo != null) viewState.setIsFavouriteSwitchState(dbRepo.isFavourite)
                val stargazersCount = repository.stargazers_count
                var page = ceil(stargazersCount / 100.0).toInt()
                while (page >= 0) {
                    val response =
                        App.gitHubApi.getStargazers(repoUserName, repoName, page, 100)
                            .execute()
                    page--
                    (response.body() ?: return@launch).forEach {
                        GlobalScope.launch {
                            if (App.db.starDao().findByStargazerUserNameAndStarredAt(
                                    it.user.login,
                                    it.starred_at
                                ) == null
                            )
                                App.db.starDao().insertAll(
                                    Star(
                                        repoUserName,
                                        repoName,
                                        it.user.login,
                                        it.starred_at
                                    )
                                )
                        }
                        val date = LocalDateTime.parse(it.starred_at.replace("Z", ""))
//                    breakNeeded = date < (currentYear.minusYears(1))
                        breakNeeded = date < startDate
                        if (!breakNeeded) {
//                        val currentUserListOfMonth = usersOfMonths[date.month]
                            val key =
                                if (date.year == currentTime.year) date.monthValue + 12 else date.monthValue
                            if (usersOfMonths[key] != null) usersOfMonths[key]!!.add(
                                it.user.login
                            )
                            else usersOfMonths[key] = mutableListOf(it.user.login)
//                            val currentAmountOfStars = months[key]
//                            months[key] =
//                                if (currentAmountOfStars != null) currentAmountOfStars + 1
//                                else 1
                        }
                    }
                    if (breakNeeded) break
                }
                showAndSave(usersOfMonths, repoName, repoUserName, xAxisStart)
                CurrentValuesStore.activity.runOnUiThread {
                    Toast.makeText(
                        CurrentValuesStore.activity, "Chart data updated!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else CurrentValuesStore.activity.runOnUiThread {
                Toast.makeText(
                    CurrentValuesStore.activity, "Offline mode",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadFromDB(
        repoName: String,
        repoUserName: String,
        startDate: LocalDateTime
    ): MutableMap<Int, MutableList<String>> {
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
//                            val currentAmountOfStars = months[key]
//                            months[key] =
//                                if (currentAmountOfStars != null) currentAmountOfStars + 1
//                                else 1
                }
            }
        return usersOfMonths
    }

    fun showAndSave(
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
            (Month.values()[(it.key - 1) % 12].name.toLowerCase().capitalize() +
                    if (it.key - 12 > 0) " (${currentTime.year})"
                    else " (${currentTime.year - 1})") to it.key
        }.toMap()
        viewState.setMonthInputAdapter(monthList.map { it.key })
    }

    fun onShowMonthButtonClicked(monthInput: String) {
        CurrentValuesStore.month = monthList[monthInput]!!
    }
}