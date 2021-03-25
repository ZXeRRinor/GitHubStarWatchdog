package com.zxerrinor.githubstarwatchdog

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.zxerrinor.githubstarwatchdog.database.AppDatabase
import com.zxerrinor.githubstarwatchdog.githubapi.GitHubApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

const val MONTH_ARGUMENT_NAME = "month"
const val MONTH_NUMBERS_ARGUMENT_NAME = "monthNumbers"
const val REPO_NAME_ARGUMENT_NAME = "repoName"
const val REPO_USER_NAME_ARGUMENT_NAME = "repoUserName"
const val OFFLINE_MODE_ARGUMENT_NAME = "offlineMode"
const val GITHUB_AUTH_TOKEN = "dd2937208d893eca075a22dddeeacf95ab419e29"

class App : Application() {
    private lateinit var retrofit: Retrofit
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).enableMultiInstanceInvalidation().build()

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        gitHubApi = retrofit.create(GitHubApi::class.java)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .setRequiresStorageNotLow(true)
            .build()
        val uploadWorkRequest: WorkRequest =
            PeriodicWorkRequestBuilder<LoadDataForFavouriteRepositoriesWorker>(
                8,
                TimeUnit.HOURS,
                20,
                TimeUnit.MINUTES
            ).setConstraints(constraints).build()
        WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)
    }

    companion object {
        lateinit var gitHubApi: GitHubApi
        lateinit var db: AppDatabase
    }
}