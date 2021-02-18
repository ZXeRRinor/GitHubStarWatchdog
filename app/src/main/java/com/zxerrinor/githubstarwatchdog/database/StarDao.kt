package com.zxerrinor.githubstarwatchdog.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StarDao {
    @Query("SELECT * FROM star")
    fun getAll(): List<Star>

    @Query("SELECT * FROM star WHERE stargazer_username IN (:stargazerUserNames)")
    fun findAllByStargazerUserNames(stargazerUserNames: Array<String>): List<Star>

    @Query("SELECT * FROM star WHERE (stargazer_username = (:stargazerUserName) AND starred_at = (:starredAt))")
    fun findByStargazerUserNameAndStarredAt(stargazerUserName: String, starredAt: String): Star?

    @Query("SELECT * FROM star WHERE (repo_name = (:repoName) AND repo_username = (:repoUserName))")
    fun findAllByRepoNameAndRepoUserName(repoName: String, repoUserName: String): List<Star>

    @Query("SELECT COUNT(*) FROM star WHERE (repo_name = (:repoName) AND repo_username = (:repoUserName))")
    fun countByRepoNameAndRepoUserName(repoName: String, repoUserName: String): Long

    @Query("SELECT * FROM star WHERE uid = (:id) LIMIT 1")
    fun findById(id: Long): Star

    @Insert
    fun insertAll(vararg repo: Star)

    @Delete
    fun delete(repo: Star)
}