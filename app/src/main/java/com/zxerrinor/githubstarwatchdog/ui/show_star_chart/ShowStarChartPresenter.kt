package com.zxerrinor.githubstarwatchdog.ui.show_star_chart

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.github.mikephil.charting.data.BarEntry
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.libs.extensions.list.toArrayList
import com.zxerrinor.githubstarwatchdog.*
import com.zxerrinor.githubstarwatchdog.database.Repository
import com.zxerrinor.githubstarwatchdog.database.Star
import com.zxerrinor.githubstarwatchdog.githubapi.Stargazer
import com.zxerrinor.githubstarwatchdog.ui.base.BasePresenter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.math.ceil

class ShowStarChartPresenter : BasePresenter<ShowStarChartView>() {
    private var monthList = mapOf<String, Byte>()

    private var months = mapOf<Byte, List<String>>()

    private val fragment: ShowStarChartFragment
        get() {
            val fragment = this.attachedViews.toList().first()
            if (fragment is ShowStarChartFragment) return fragment
            else throw IllegalStateException("Illegal object")
        }

    fun onCurrentRepoFavouriteSwitchClicked() = GlobalScope.launch {
        val repoUserName = fragment.arguments?.getString(REPO_USER_NAME_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$REPO_USER_NAME_ARGUMENT_NAME not found in arguments")
        val repoName = fragment.arguments?.getString(REPO_NAME_ARGUMENT_NAME)
            ?: throw IllegalArgumentException("$REPO_NAME_ARGUMENT_NAME not found in arguments")
        val repo = App.db.repositoryDao().findByRepoNameAndRepoUserName(
            repoName,
            repoUserName
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
            val activity =
                fragment.activity ?: throw IllegalStateException("Fragment must be in activity")

            val repoUserName = fragment.arguments?.getString(REPO_USER_NAME_ARGUMENT_NAME)
                ?: throw IllegalArgumentException("$REPO_USER_NAME_ARGUMENT_NAME not found in arguments")
            val repoName = fragment.arguments?.getString(REPO_NAME_ARGUMENT_NAME)
                ?: throw IllegalArgumentException("$REPO_NAME_ARGUMENT_NAME not found in arguments")
            val currentTime = LocalDateTime.now()
            val startDate =
                currentTime.minusMonths(11).minusDays(currentTime.dayOfMonth.toLong() - 1)
            val xAxisStart = startDate.monthValue
            var usersOfMonths = loadFromDb(repoName, repoUserName, startDate)
            prepareDataForChartAndMonthInputAdapter(
                usersOfMonths,
                repoName,
                repoUserName,
                xAxisStart
            )
            val offlineMode = fragment.arguments?.getBoolean(OFFLINE_MODE_ARGUMENT_NAME)
                ?: throw IllegalArgumentException("$OFFLINE_MODE_ARGUMENT_NAME not found in arguments")
            if (isInternetAvailable() && !offlineMode) {
                usersOfMonths = loadFromGitHubAndSaveToDb(repoUserName, repoName, startDate)
                prepareDataForChartAndMonthInputAdapter(
                    usersOfMonths,
                    repoName,
                    repoUserName,
                    xAxisStart
                )
                showUpdateStatusToast(true, activity)
            } else showUpdateStatusToast(false, activity)
        }
    }

    private fun showUpdateStatusToast(
        updated: Boolean,
        activity: Activity = fragment.activity
            ?: throw IllegalStateException("Fragment must be in activity")
    ) {
        val toastText = if (updated) "Chart data updated!" else "Offline mode"
        activity.runOnUiThread {
            Toast.makeText(
                activity, toastText,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadFromGitHubAndSaveToDb(
        repoUserName: String,
        repoName: String,
        startDate: LocalDateTime
    ): Map<Byte, MutableList<String>> {
        val repository =
            App.gitHubApi.getRepositoryInfo(repoUserName, repoName).execute().body()
                ?: throw IllegalArgumentException("Not found repository with such name")
        val dbRepo = App.db.repositoryDao().findById(repository.id)
        if (dbRepo != null) viewState.setIsFavouriteSwitchState(dbRepo.isFavourite)
        var page = ceil(repository.stargazers_count / 100.0).toInt()
        val usersOfMonths = mutableMapOf<Byte, MutableList<String>>()
        var breakNeeded = false
        while (page >= 0 && !breakNeeded) {
            val response =
                App.gitHubApi
                    .getStargazers(repoUserName, repoName, page, 100).execute()
            page--
            (response.body()
                ?: throw IllegalArgumentException("Not found stargazers for this repository")).forEach {
                saveToDb(repoUserName, repoName, it)
                val date = LocalDateTime.parse(it.starred_at.replace("Z", ""))
                if (date >= startDate) {
                    val key =
                        (if (date.year == LocalDateTime.now().year) date.monthValue + 12 else date.monthValue).toByte()
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
    ): Map<Byte, MutableList<String>> {
        val usersOfMonths = mutableMapOf<Byte, MutableList<String>>()
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
                        (if (date.year == currentTime.year) date.monthValue + 12 else date.monthValue).toByte()
                    if (usersOfMonths[key] != null) usersOfMonths[key]!!.add(
                        it.stargazerUsername
                    )
                    else usersOfMonths[key] = mutableListOf(it.stargazerUsername)
                }
            }
        return usersOfMonths
    }

    private fun prepareDataForChartAndMonthInputAdapter(
        usersOfMonths: Map<Byte, MutableList<String>>,
        repoName: String,
        repoUserName: String,
        xAxisStart: Int
    ) {
        val currentTime = LocalDateTime.now()
        months = usersOfMonths
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
        viewState.setMonthSpinnerContent(monthList.map { it.key })
    }

    fun onShowMonthButtonClicked(monthInput: String) {
        val bundle = Bundle()
        bundle.putByteArray(
            MONTH_NUMBERS_ARGUMENT_NAME,
            months.keys.toList().map { it }.toByteArray()
        )
        months.forEach { bundle.putStringArrayList("${it.key}", it.value.toArrayList()) }
        bundle.putByte(MONTH_ARGUMENT_NAME, monthList[monthInput]!!)
        findNavController(fragment).navigate(
            R.id.action_ShowStarChartFragment_to_ShowUserListOfMonthFragment,
            bundle
        )
    }
}