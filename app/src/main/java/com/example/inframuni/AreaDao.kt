package com.example.inframuni

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AreaDao {
    @Query("SELECT * FROM area")
    fun getAll(): List<Area>

    @Insert
    fun insert(area: Area)
}