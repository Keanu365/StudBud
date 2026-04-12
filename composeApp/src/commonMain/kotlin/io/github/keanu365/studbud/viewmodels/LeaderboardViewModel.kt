package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    val user: User
): ViewModel() {
    private val _groups = MutableStateFlow(emptyList<Group>())
    val groups = _groups.asStateFlow()

    private val _selectedGroup = MutableStateFlow(Group("to be discarded", "Loading..."))
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _groupMembers = MutableStateFlow(emptyList<User>())
    val groupMembers = _groupMembers.asStateFlow()

    init {
        viewModelScope.launch {
            user.groups?.forEachIndexed { index, groupId ->
                _groups.value += supabase.from("groups")
                    .select {
                        filter {
                            eq("id", groupId)
                        }
                    }
                    .decodeSingle<Group>()
                    .also{if (index == 0) setGroup(it)}
            }
        }
    }

    fun setGroup(group: Group) = viewModelScope.launch {
        try {
            _selectedGroup.value = group
            _groupMembers.value = emptyList()
            val newGroupMembers = mutableListOf<User>()
            group.members.forEach { userId ->
                newGroupMembers += supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<User>()
            }
            _groupMembers.value = newGroupMembers.sortedByDescending { it.all_time_studs }
        } catch (_: Exception) {
            println("Something went wrong while loading group members. Check internet connection.")
        }
    }
}