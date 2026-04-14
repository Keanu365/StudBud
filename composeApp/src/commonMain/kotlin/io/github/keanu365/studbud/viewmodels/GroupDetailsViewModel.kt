package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class GroupDetailsViewModel(
    val group: Group
): ViewModel() {
    protected val _members = MutableStateFlow<List<User>>(emptyList())
    val members = _members.asStateFlow()

    protected val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments = _assignments.asStateFlow()

    private var isRefreshing = false
    suspend fun refresh(includeOwner: Boolean = true){
        if (isRefreshing) return
        try {
            isRefreshing = true
            val newMembers = mutableListOf<User>()
            group.members.forEach { userId ->
                val user = supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<User>()
                if (includeOwner || user.id != group.owner) newMembers.add(user)
            }
            _members.emit(newMembers)

            val newAssignments = mutableListOf<Assignment>()
            group.assignments.forEach { assignmentId ->
                val assignment = supabase.from("assignments")
                    .select {
                        filter {
                            eq("id", assignmentId)
                        }
                    }
                    .decodeSingle<Assignment>()
                newAssignments.add(assignment)
            }
            _assignments.emit(newAssignments)
        } catch(e: Exception){
            e.printStackTrace()
        } finally {
            isRefreshing = false
        }
    }

    open fun onInit(){
        viewModelScope.launch { refresh() }
    }
    init {
        onInit()
    }
}