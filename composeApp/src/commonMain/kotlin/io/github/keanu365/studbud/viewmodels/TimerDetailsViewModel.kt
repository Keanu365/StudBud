package io.github.keanu365.studbud.viewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoUserAssignment
import io.github.keanu365.studbud.ErrorButton
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

class TimerDetailsViewModel(
    startingAssignment: Assignment?,
    val onStartSaved: (UserAssignment) -> Unit
): AlertViewModel() {
    private val _period = MutableStateFlow("25")
    private val _periodError = MutableStateFlow(false)
    val period = _period.asStateFlow()
    val periodError = _periodError.asStateFlow()
    val setPeriod = { period: String ->
        _period.value = period
    }
    private val _breaktime = MutableStateFlow("5")
    private val _breaktimeError = MutableStateFlow(false)
    val breaktime = _breaktime.asStateFlow()
    val breaktimeError = _breaktimeError.asStateFlow()
    val setBreaktime = { breaktime: String ->
        _breaktime.value = breaktime
    }
    private val _iterations = MutableStateFlow("1")
    private val _iterationsError = MutableStateFlow(false)
    val iterations = _iterations.asStateFlow()
    val iterationsError = _iterationsError.asStateFlow()
    val setIterations = { iterations: String ->
        _iterations.value = iterations
    }

    private val _selectedAssignment = MutableStateFlow(startingAssignment)
    private val _assignmentsExpanded = MutableStateFlow(false)
    val selectedAssignment = _selectedAssignment.asStateFlow()
    val assignmentsExpanded = _assignmentsExpanded.asStateFlow()
    val setSelectedAssignment = { assignment: Assignment? ->
        _selectedAssignment.value = assignment
        _assignmentsExpanded.value = false
    }
    fun toggleAssignments(
        show: Boolean = !_assignmentsExpanded.value
    ){
        _assignmentsExpanded.value = show
    }

    fun checkDetails(): AutoUserAssignment? {
        val period = _period.value.ifEmpty {
            _period.value = "0"
            "0"
        }.filter{it.isDigit()}.toInt()
        val breaktime = _breaktime.value.ifEmpty {
            _breaktime.value = "0"
            "0"
        }.filter{it.isDigit()}.toInt()
        val iterations = _iterations.value.ifEmpty {
            _iterations.value = "0"
            "0"
        }.filter{it.isDigit()}.toInt()

        _periodError.value = period <= 0 || period < breaktime + 5
        _breaktimeError.value = breaktime <= 0 || period < breaktime + 5
        _iterationsError.value = iterations <= 0

        val hasError = _periodError.value || _breaktimeError.value || _iterationsError.value
        return if (!hasError) AutoUserAssignment(
            assignment_id = _selectedAssignment.value?.id ?: "",
            period = period,
            breaktime = breaktime,
            iterations = iterations
        ) else null
    }

    private val _savedUserAssignments = MutableStateFlow<List<UserAssignment>>(emptyList())
    private val _savedAssignments = MutableStateFlow<List<Assignment>>(emptyList())
    private val _selectedSavedAssignment = MutableStateFlow<UserAssignment?>(null)
    private val _savedAssignmentsExpanded = MutableStateFlow(false)

    suspend fun getSavedAssignments(){
        try {
            _savedUserAssignments.value = supabase.from("user_assignments")
                .select {
                    filter {
                        eq("completed", false)
                        supabase.auth.currentUserOrNull()?.let{ eq("user_id", it.id) }
                    }
                }
                .decodeList<UserAssignment>()
                .also { newList ->
                    newList.forEach { userAssignment ->
                        _savedAssignments.value += supabase.from("assignments")
                            .select {
                                filter {
                                    eq("id", userAssignment.assignment_id)
                                }
                            }
                            .decodeSingle<Assignment>()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        viewModelScope.launch { getSavedAssignments() }
    }

    fun showSavedAssignments(){
        _alert.value = { SelectDialog() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SelectDialog(){
        AlertDialog(
            onDismissRequest = {
                _alert.value = {}
            },
            title = { Text("Saved Assignments") },
            text = {
                val isExpanded = _savedAssignmentsExpanded.collectAsState()
                Column {
                    Text("Please select a saved session from the dropdown menu.")
                    Spacer(Modifier.height(10.dp))
                    if (_savedUserAssignments.value.isNotEmpty()) ExposedDropdownMenuBox(
                        expanded = isExpanded.value,
                        onExpandedChange = {
                            _savedAssignmentsExpanded.value = !_savedAssignmentsExpanded.value
                        },
                    ){
                        InfoField(
                            value = _savedAssignments.value.find { it.id == _selectedSavedAssignment.value?.assignment_id }?.name
                                ?: "None",
                            onValueChange = {},
                            readOnly = true,
                            isError = false,
                            labelText = "Assignment",
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded.value)
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = _savedAssignmentsExpanded.value,
                            onDismissRequest = {
                                _savedAssignmentsExpanded.value = false
                            },
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                        ){
                            _savedUserAssignments.value.forEach { assignment ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = _savedAssignments.value.find { it.id == assignment.assignment_id }?.name
                                                ?: "None"
                                        )
                                    },
                                    onClick = {
                                        _selectedSavedAssignment.value = assignment
                                        _savedAssignmentsExpanded.value = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    } else Text("You currently have no saved sessions.")
                }
            },
            confirmButton = {
                ErrorButton(
                    onClick = {
                        _alert.value = {}
                        _selectedSavedAssignment.value?.let{
                            onStartSaved(it)
                            _selectedSavedAssignment.value = null
                        }
                    }
                ){
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        _alert.value = {}
                    },
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
}