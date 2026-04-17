package io.github.keanu365.studbud

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val avatar_url: String = "",
    val studs: Int = 0,
    val all_time_studs: Int = 0,
    val groups: List<String>? = null,
    val achievements: List<Int>? = null,
    val fcm_token: String? = null
)
@Serializable
data class Group(
    val id: String,
    val name: String,
    val description: String = "",
    val members: List<String> = emptyList(),
    val owner: String = "",
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
@Serializable
data class AutoAssignment(
    val name: String,
    val due_date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val group_id: String,
    val description: String = "",
)
@Serializable
data class UserAssignment(
    val uuid: String = "",
    val assignment_id: String = "",
    val created_at: Instant = Clock.System.now(),
    val user_id: String,
    val period: Int = 25,
    val breaktime: Int = 5,
    val iterations: Int = 1,
    val completed: Boolean = false
)
@Serializable
data class AutoUserAssignment(
    val assignment_id: String,
    val period: Int = 25,
    val breaktime: Int = 5,
    val iterations: Int = 1
)
@Serializable
data class Achievement(
    val id: Int,
    val name: String,
    val description: String = "",
    val requirement: String = "",
    val secret: Boolean = false,
    val badge_url: String = ""
)