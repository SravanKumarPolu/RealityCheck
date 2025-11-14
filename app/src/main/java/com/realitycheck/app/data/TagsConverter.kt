package com.realitycheck.app.data

import androidx.room.TypeConverter

/**
 * Type converter for tags (List<String>)
 * Stores tags as comma-separated string in database
 */
class TagsConverter {
    @TypeConverter
    fun fromTags(tags: List<String>?): String? {
        return tags?.joinToString(",") ?: ""
    }
    
    @TypeConverter
    fun toTags(tagsString: String?): List<String> {
        if (tagsString.isNullOrBlank()) return emptyList()
        return tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

