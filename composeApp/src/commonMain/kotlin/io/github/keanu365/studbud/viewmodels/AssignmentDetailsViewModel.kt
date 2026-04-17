package io.github.keanu365.studbud.viewmodels

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
    val assignment: Assignment,
    val onDelete: () -> Unit
): AlertViewModel() {
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

    fun showDeleteAlert(){
        _alert.value = {
            Alert(
                title = "Delete Assignment",
                text = "Are you sure you want to delete this assignment?",
            ){
                viewModelScope.launch {
                    _alert.value = {}
                    deleteAssignment()
                }
            }
        }
    }
    suspend fun deleteAssignment(){
        try {
            _group.value?.let{
                supabase.from("groups")
                    .update(
                        {
                            set("assignments", it.assignments.minus(assignment.id))
                        }
                    ){
                        filter {
                            eq("id", it.id)
                        }
                    }
            }
            supabase.from("assignments").delete {
                filter {
                    eq("id", assignment.id)
                }
            }
            supabase.from("user_assignments").delete {
                filter {
                    eq("assignment_id", assignment.id)
                    eq("completed", false)
                }
            }
            onDelete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        viewModelScope.launch { getGroup() }
    }

    private val createdAt = assignment.created_at.toLocalDateTime(TimeZone.currentSystemDefault())
    val createDate = "${createdAt.date.day}/${createdAt.date.month.number}/${createdAt.date.year}"
    val createTime = "${createdAt.time.hour}:${createdAt.time.minute}"
}