package io.github.keanu365.studbud.viewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class GroupDetailsViewModel(
    val group: Group,
    val user: User? = null,
    val onFinish: (String) -> Unit = {}
): AlertViewModel() {
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

    fun showDeleteAlert(){
        _alert.value = {
            Alert(
                title = "Group Deletion",
                text = "Are you sure you want to delete this group?\nThis action cannot be undone!",
                onConfirm = {
                    _alert.value = { ConfirmDialog() }
                }
            )
        }
    }
    private suspend fun deleteGroup(){
        try {
            _members.value.forEach { member ->
                supabase.from("profiles")
                    .update(
                        {
                            set("groups", member.groups?.minus(group.id))
                        }
                    ) {
                        filter {
                            eq("id", member.id)
                        }
                    }
            }
            _assignments.value.forEach { assignment ->
                supabase.from("assignments").delete {
                    filter {
                        eq("id", assignment.id)
                    }
                }
            }
            supabase.from("groups").delete {
                filter {
                    eq("id", group.id)
                }
            }
            onFinish("Group Deleted Successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLeaveAlert(){
        _alert.value = {
            Alert(
                title = "Leave Group",
                text = "Are you sure you want to leave this group?",
                onConfirm = {
                    viewModelScope.launch { leaveGroup() }
                }
            )
        }
    }
    private suspend fun leaveGroup(){
        try {
            user?.let{
                supabase.from("profiles")
                    .update(
                        {
                            set("groups", it.groups?.minus(group.id))
                        }
                    ){
                        filter {
                            eq("id", it.id)
                        }
                    }
                supabase.from("groups")
                    .update(
                        {
                            set("members", group.members.minus(it.id))
                        }
                    ) {
                        filter {
                            eq("id", group.id)
                        }
                    }
                onFinish("Successfully left group!")
                NotifierManager.getPushNotifier().unSubscribeFromTopic("group_${group.id}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    private fun ConfirmDialog(){
        var confirmCode by remember { mutableStateOf("") }
        var confirmError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {_alert.value = {}},
            title = {Text("Delete Confirmation")},
            text = {
                Column {
                    Text("Please enter the group code below to confirm deletion.")
                    InfoField(
                        value = confirmCode,
                        onValueChange = { confirmCode = it },
                        labelText = "Group Code",
                        isError = confirmError,
                        errorText = "Incorrect code."
                    )
                }
            },
            confirmButton = {
                TertiaryButton(
                    onClick = {
                        confirmError = confirmCode != group.id
                        if (!confirmError) viewModelScope.launch {
                            deleteGroup()
                        }
                    }
                ){
                    Text("Confirm")
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
                    Text("Cancel")
                }
            }
        )
    }

    open fun onInit(){
        viewModelScope.launch { refresh() }
    }
    init {
        onInit()
    }
}