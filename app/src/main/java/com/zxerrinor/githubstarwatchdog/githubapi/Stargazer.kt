package com.zxerrinor.githubstarwatchdog.githubapi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stargazer(
    val starred_at: String,
    val user: User
)