package com.zxerrinor.githubstarwatchdog

import com.zxerrinor.githubstarwatchdog.ui.MainActivity

object CurrentValuesStore {
    lateinit var activity: MainActivity
    var repoUserName = ""
    var repoName = ""
    var month = 0
    var months = mapOf<Int, List<String>>()
    var repositoriesOfUser: List<String>? = null
    const val gitHubAuthToken = "5817ce9000eddf49a0e350daa12902271a8e91bf"
    var offlineMode = false
}
