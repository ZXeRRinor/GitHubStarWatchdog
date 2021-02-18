package com.zxerrinor.githubstarwatchdog.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RepositoryDao {
    @Query("SELECT * FROM repository")
    fun getAll(): List<Repository>

    @Query("SELECT * FROM repository WHERE is_favourite = (:isFavourite)")
    fun getAllByFavouriteState(isFavourite: Boolean): List<Repository>

    @Query("SELECT * FROM repository WHERE repo_username = (:repoUserName)")
    fun findAllByRepoUserName(repoUserName: String): List<Repository>

    @Query("SELECT * FROM repository WHERE repo_id = (:id) LIMIT 1")
    fun findById(id: Long): Repository?

    @Query("SELECT * FROM repository WHERE (repo_name = (:repoName) AND repo_username = (:repoUserName)) LIMIT 1")
    fun findByRepoNameAndRepoUserName(repoName: String, repoUserName: String): Repository?

    @Insert
    fun insertAll(vararg repo: Repository)

    @Delete
    fun delete(repo: Repository)
}
