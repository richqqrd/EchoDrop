package com.example.echodrop.model.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.example.echodrop.model.domain.TransferState
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromTransferState(value: TransferState): String {
        return value.name
    }

    @TypeConverter
    fun toTransferState(value: String): TransferState {
        return TransferState.valueOf(value)
    }
    }