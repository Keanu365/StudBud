package io.github.keanu365.studbud

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val avatar_url: String = "",
    val studs: Int = 0,
    val all_time_studs: Int = 0,
    val groups: List<String>? = null
)
@Serializable
data class Group(
    val id: String,
    val name: String,
    val description: String,
    val members: List<String> = emptyList(),
    val assignments: List<String> = emptyList()
)
@Serializable
data class Assignment(
    val id: String,
    val name: String,
    val created_at: Instant,
    val due_date: LocalDate,
    val group_id: String,
    val description: String,
)