package com.example.inframuni

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint

@Entity
data class Area(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val points: List<GeoPoint>
)