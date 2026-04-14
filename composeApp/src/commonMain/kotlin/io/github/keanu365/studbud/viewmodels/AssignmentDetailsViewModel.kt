package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class AssignmentDetailsViewModel(
    val assignment: Assignment
): ViewModel() {
    private val _group = MutableStateFlow<Group?>(null)
    val group = _group.asStateFlow()

    private var isGetting = false
    suspend fun getGroup(){
        if (isGetting) return
        try {
            isGetting = true
            _group.value = supabase.from("groups")
                .select {
                    filter {
                        eq("id", assignment.group_id)
                    }
                }
                .decodeSingleOrNull<Group>()
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            isGetting = false
        }
    }

    init {
        viewModelScope.launch { getGroup() }
    }

    private val createdAt = assignment.created_at.toLocalDateTime(TimeZone.currentSystemDefault())
    val createDate = "${createdAt.date.day}/${createdAt.date.month.number}/${createdAt.date.year}"
    val createTime = "${createdAt.time.hour}:${createdAt.time.minute}"
}