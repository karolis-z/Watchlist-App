package com.myapplications.mywatchlist.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genre-entity")
data class GenreEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String
)
