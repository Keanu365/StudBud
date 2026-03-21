package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoAssignment
import io.github.keanu365.studbud.DatePickerModal
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.toLocalDateTime
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentPage(
    user: User,
    startingGroup: Group = Group(user.id, "Personal"),
    onAdd: (Assignment) -> Unit,
    showSnackBar: (String) -> Unit
){
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by rememberNetworkStatus()

    val groups = remember { mutableStateListOf(startingGroup) }
    var selectedGroup by remember {mutableStateOf(startingGroup)}
    var selectedGroupName by remember {mutableStateOf(startingGroup.name)}
    var isGroupExpanded by remember {mutableStateOf(false)}
    LaunchedEffect(Unit){
        user.groups?.forEach { groupId ->
            try {
                supabase.from("groups")
                    .select {
                        filter {
                            eq("id", groupId)
                        }
                    }
                    .decodeSingleOrNull<Group>()?.let {
                        groups.add(it)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val dueDate = rememberDatePickerState()
    var nameError by remember { mutableStateOf(false) }
    var submitAttempted by remember {mutableStateOf(false)}
    LaunchedEffect(name, description, selectedGroup, dueDate){
        if (submitAttempted) {
            nameError = name.isBlank()
        }
    }
    fun createAssignment() = run {
        if (networkStatus != NetworkStatus.Available) showSnackBar("You're not connected to a network! Please check your connection and try again.")
        else coroutineScope.launch {
            try {
                val autoAssignment = AutoAssignment(
                    name = name,
                    due_date = dueDate.selectedDateMillis.let {
                        Instant.fromEpochMilliseconds(it!!).toLocalDateTime(UTC).date
                    },
                    group_id = selectedGroup.id,
                    description = description
                )
                val assignment = supabase.from("assignments").insert(autoAssignment){
                    filter {
                        eq("id", user.id)
                    }
                }.decodeSingle<Assignment>()
                onAdd(assignment)
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar("Assignment creation failed.")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ){
        TitleText("Add Assignment")
        InfoField(
            value = name,
            onValueChange = {name = it},
            labelText = "Name",
            isError = nameError,
            errorText = "Please enter an assignment name!"
        )
        DatePickerModal(
            title = "Due Date",
            onDateSelected = {
                dueDate.selectedDateMillis = it
            },
        )
        ExposedDropdownMenuBox(
            expanded = isGroupExpanded,
            onExpandedChange = { isGroupExpanded = !isGroupExpanded },
        ){
            OutlinedTextField(
                value = selectedGroupName,
                onValueChange = {selectedGroupName = it},
                label = {Text("Group")},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            ExposedDropdownMenu(
                expanded = isGroupExpanded,
                onDismissRequest = { isGroupExpanded = false },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .height(300.dp)
            ){
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = {Text(group.name)},
                        onClick = {
                            selectedGroup = group
                            isGroupExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        InfoField(
            value = description,
            onValueChange = { description = it },
            labelText = "Description (optional)",
            isError = false,
            errorText = "Something went wrong.",
            singleLine = false,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
        )
        TertiaryButton(
            onClick = {
                submitAttempted = true
                if (!nameError) createAssignment()
            },
        ){
            Text(
                text = "Add Assignment",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}