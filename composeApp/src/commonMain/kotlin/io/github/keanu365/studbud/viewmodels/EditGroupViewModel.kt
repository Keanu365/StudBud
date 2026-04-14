package io.github.keanu365.studbud.viewmodels

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.ErrorButton
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_warning

class EditGroupViewModel(
    group: Group
): GroupDetailsViewModel(group) {

    private val _newGroup = MutableStateFlow(group)
    val newGroup = _newGroup.asStateFlow()

    private val usersToRemove = mutableListOf<User>()
    private val assignmentsToRemove = mutableListOf<Assignment>()

    fun setName(name: String){
        _newGroup.value = _newGroup.value.copy(name = name)
    }
    fun setDescription(desc: String){
        _newGroup.value = _newGroup.value.copy(description = desc)
    }

    private val _alert = MutableStateFlow<@Composable () -> Unit>({})
    val alert = _alert.asStateFlow()

    fun showAlert(user: User){
        _alert.value = {
            Alert(
                title = "User Removal",
                text = "Are you sure you want to remove ${user.username}" +
                        " from this group?\nThis action cannot be undone!"
            ){
                _members.value -= user
                _newGroup.value = _newGroup.value.copy(
                    members = _newGroup.value.members.filter { it != user.id }
                )
                usersToRemove.add(user)
            }
        }
    }
    fun showAlert(assignment: Assignment){
        _alert.value = {
            Alert(
                title = "Assignment Deletion",
                text = "Are you sure you want to remove the assignment" +
                " '${assignment.name}' from this group?\nThis action cannot be undone!"
            ){
                _assignments.value -= assignment
                _newGroup.value = _newGroup.value.copy(
                    assignments = _newGroup.value.assignments.filter { it != assignment.id }
                )
                assignmentsToRemove.add(assignment)
            }
        }
    }

    @Composable
    private fun Alert(
        title: String,
        text: String,
        onConfirm: () -> Unit
    ){
        AlertDialog(
            onDismissRequest = {_alert.value = {}},
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_warning),
                    contentDescription = null
                )
            },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                ErrorButton(
                    onClick = {
                        _alert.value = {}
                        onConfirm()
                    }
                ){
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { _alert.value = {} },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ){
                    Text("No")
                }
            }
        )
    }

    fun saveGroup(){
        viewModelScope.launch {
            try {
                // Update group desc first so empty string is captured
                supabase.from("groups").update({
                    set("name", _newGroup.value.name)
                    set("description", _newGroup.value.description) // This forces the empty string
                }) {
                    filter { eq("id", _newGroup.value.id) }
                }
                supabase.from("groups").upsert(_newGroup.value)
                usersToRemove.forEach { user ->
                    supabase.from("profiles")
                        .update (
                            {
                                set("groups", user.groups?.minus(group.id))
                            }
                        ){
                            filter {
                                eq("id", user.id)
                            }
                        }
                }
                usersToRemove.clear()
                assignmentsToRemove.forEach { assignment ->
                    supabase.from("assignments")
                        .delete {
                            filter {
                                eq("id", assignment.id)
                            }
                        }
                }
                assignmentsToRemove.clear()
                println("Group updated!")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Group update failed.")
            }
        }
    }

    override fun onInit() {
        viewModelScope.launch {
            refresh(includeOwner = false)
        }
    }
}