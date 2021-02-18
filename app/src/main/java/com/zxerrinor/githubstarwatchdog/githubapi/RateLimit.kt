package com.zxerrinor.githubstarwatchdog.githubapi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Limit(
    val limit: Int,
    val remaining: Int,
    val reset: Long,
    val used: Int
)

@JsonClass(generateAdapter = true)
data class Resources(
    val core: Limit,
    val graphql: Limit,
    val integration_manifest: Limit,
    val search: Limit,
)

@JsonClass(generateAdapter = true)
data class RateLimit(
    val resources: Resources,
    val rate: Limit
)