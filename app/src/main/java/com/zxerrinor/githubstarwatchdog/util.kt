package com.zxerrinor.githubstarwatchdog

import android.widget.Toast
import com.omegar.mvp.MvpAppCompatActivity
import com.zxerrinor.githubstarwatchdog.database.Star
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.time.LocalDateTime
import kotlin.math.ceil

fun isInternetAvailable(): Boolean {
    return try {
        val ipAddress: InetAddress = InetAddress.getByName("google.com")
        !ipAddress.equals("")
    } catch (e: Exception) {
        false
    }
}

fun clearDatabaseCache(activity: MvpAppCompatActivity) = GlobalScope.launch {
    val repoDao = App.db.repositoryDao()
    val starDao = App.db.starDao()
    repoDao.getAllByFavouriteState(false).forEach { repo ->
        if (!repo.isFavourite) {
            starDao.findAllByRepoNameAndRepoUserName(repo.repoName, repo.repoUserName).forEach {
                starDao.delete(it)
            }
            repoDao.delete(repo)
        }
    }
    activity.runOnUiThread {
        Toast.makeText(
            activity,
            "Cache cleared",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun loadAndSaveRepoStargazers(repoUserName: String, repoName: String) {
    val currentYear = LocalDateTime.now()
    val startDate =
        currentYear.minusMonths(11).minusDays(currentYear.dayOfMonth.toLong() - 1)
    var breakNeeded = false
    val repo =
        App.gitHubApi.getRepositoryInfo(repoUserName, repoName).execute()
    val repository = repo.body() ?: return
    val stargazersCount = repository.stargazers_count
    var page = ceil(stargazersCount / 100.0).toInt()
    while (page >= 0) {
        val response =
            App.gitHubApi.getStargazers(repoUserName, repoName, page, 100)
                .execute()
        page--
        (response.body() ?: return).forEach {
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
            val date = LocalDateTime.parse(it.starred_at.replace("Z", ""))
            breakNeeded = date < startDate
        }
        if (breakNeeded) break
    }
}