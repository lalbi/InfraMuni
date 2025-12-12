package com.example.inframuni

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromGeoPointList(value: List<GeoPoint>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGeoPointList(value: String): List<GeoPoint> {
        val listType = object : TypeToken<List<GeoPoint>>() {}.type
        return gson.fromJson(value, listType)
    }
}