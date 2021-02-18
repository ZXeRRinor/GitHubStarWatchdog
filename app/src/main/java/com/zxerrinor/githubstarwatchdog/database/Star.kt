package com.zxerrinor.githubstarwatchdog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Star(
    @ColumnInfo(name = "repo_username")
    val repoUserName: String,
    @ColumnInfo(name = "repo_name")
    val repoName: String,
    @ColumnInfo(name = "stargazer_username")
    val stargazerUsername: String,
    @ColumnInfo(name = "starred_at")
    val starredAt: String,
    @PrimaryKey(autoGenerate = true)
    val uid: Long = 0
)