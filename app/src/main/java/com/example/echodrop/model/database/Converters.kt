package com.example.echodrop.model.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.example.echodrop.model.domain.TransferState
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken

/**
 * Converters for custom data types used in the Room database.
 *
 * This class provides methods to convert complex data types to and from
 * formats that can be stored in the database, such as JSON strings.
 */
class Converters {
    private val gson = Gson()

    /**
     * Converts a list of strings to a JSON string for database storage.
     *
     * @param value The list of strings to convert.
     * @return A JSON string representation of the list.
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else gson.toJson(value)
    }

    /**
     * Converts a JSON string back to a list of strings.
     *
     * @param value The JSON string to convert.
     * @return A list of strings represented by the JSON string.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    /**
     * Converts a `TransferState` enum to a string for database storage.
     *
     * @param value The `TransferState` to convert.
     * @return The name of the `TransferState` as a string.
     */
    @TypeConverter
    fun fromTransferState(value: TransferState): String {
        return value.name
    }

    /**
     * Converts a string back to a `TransferState` enum.
     *
     * @param value The string to convert.
     * @return The `TransferState` represented by the string.
     */
    @TypeConverter
    fun toTransferState(value: String): TransferState {
        return TransferState.valueOf(value)
    }
    }