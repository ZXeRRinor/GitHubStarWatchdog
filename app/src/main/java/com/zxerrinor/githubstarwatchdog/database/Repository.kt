package com.zxerrinor.githubstarwatchdog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Repository(
    @PrimaryKey
    @ColumnInfo(name = "repo_id")
    val repoId: Long,
    @ColumnInfo(name = "repo_username")
    val repoUserName: String,
    @ColumnInfo(name = "repo_name")
    val repoName: String,
    @ColumnInfo(name = "stars")
    val stars: Int,
    @ColumnInfo(name = "is_favourite")
    val isFavourite: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)