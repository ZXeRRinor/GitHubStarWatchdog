package com.zxerrinor.githubstarwatchdog

import android.R.drawable.sym_def_app_icon
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zxerrinor.githubstarwatchdog.ui.MainActivity

class LoadDataForFavouriteRepositoriesWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        if (!isInternetAvailable()) return Result.retry()
        val rateLimitResponse =
            (App.gitHubApi.getRateLimit().execute().body() ?: return Result.retry())
        if (rateLimitResponse.rate.remaining < 10) return Result.retry()

        App.db.repositoryDao().getAllByFavouriteState(true).forEach {
            loadAndSaveRepoStargazers(it.repoUserName, it.repoName)
        }

        val intent = Intent(this.applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this.applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(this.applicationContext, "mainChannel")
            .setSmallIcon(sym_def_app_icon)
            .setContentTitle("GitHub Stars Updated")
            .setContentText("Successfully updated stars of your favourite repositories!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            this.applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.notify(1, builder.build())

        return Result.success()
    }
}
