package com.zxerrinor.githubstarwatchdog

import com.zxerrinor.githubstarwatchdog.ui.MainActivity

object CurrentValuesStore {
    lateinit var activity: MainActivity
    var repoUserName = ""
    var repoName = ""
    var month = 0
    var months = mapOf<Int, List<String>>()
    var repositoriesOfUser: List<String>? = null
    const val gitHubAuthToken = "dd2937208d893eca075a22dddeeacf95ab419e29"
    var offlineMode = false
}
