package com.zxerrinor.githubstarwatchdog.githubapi

import com.zxerrinor.githubstarwatchdog.GITHUB_AUTH_TOKEN
import retrofit2.Call
import retrofit2.http.*

interface GitHubApi {
    @GET("/repos/{user}/{repo}/stargazers")
    @Headers("Accept: application/vnd.github.v3.star+json")
    fun getStargazers(
        @Path("user") userName: String,
        @Path("repo") repoName: String,
        @Query("page") page: Int,
        @Query("per_page") resultsInPage: Int,
        @Header("Authorization") authToken: String = "token $GITHUB_AUTH_TOKEN"
    ): Call<List<Stargazer>>

    @GET("/repos/{user}/{repo}")
    @Headers("Accept: application/vnd.github.v3.star+json")
    fun getRepositoryInfo(
        @Path("user") userName: String,
        @Path("repo") repoName: String,
        @Header("Authorization") authToken: String = "token $GITHUB_AUTH_TOKEN"
    ): Call<Repository>

    @GET("/users/{user}/repos")
    @Headers("Accept: application/vnd.github.v3.star+json")
    fun getRepositoriesOfUser(
        @Path("user") userName: String,
        @Query("page") page: Int,
        @Query("per_page") resultsInPage: Int,
        @Header("Authorization") authToken: String = "token $GITHUB_AUTH_TOKEN"
    ): Call<List<Repository>>

    @GET("/users/{user}")
    @Headers("Accept: application/vnd.github.v3.star+json")
    fun getUser(
        @Path("user") userName: String,
        @Header("Authorization") authToken: String = "token $GITHUB_AUTH_TOKEN"
    ): Call<User>

    @GET("/rate_limit")
    @Headers("Accept: application/vnd.github.v3.star+json")
    fun getRateLimit(
        @Header("Authorization") authToken: String = "token $GITHUB_AUTH_TOKEN"
    ): Call<RateLimit>
}