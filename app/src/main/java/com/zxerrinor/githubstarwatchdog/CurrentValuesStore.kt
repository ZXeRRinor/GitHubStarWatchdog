package com.zxerrinor.githubstarwatchdog

import com.zxerrinor.githubstarwatchdog.ui.MainActivity

object CurrentValuesStore {
    lateinit var activity: MainActivity
    var repoUserName = ""
    var repoName = ""
    var month = 0
    var months = mapOf<Int, List<String>>()
    var repositoriesOfUser: List<String>? = null
    const val gitHubAuthToken = "7c9e3f67516abbecd81f8f01e2d3ff6e61926a13"
    var offlineMode = false
}
