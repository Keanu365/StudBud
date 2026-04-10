package io.github.keanu365.studbud.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    startingGroup: Group? = null,
    onAdd: (Assignment) -> Unit,
    showSnackBar: (String) -> Unit
){
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by rememberNetworkStatus()

    val groups = remember { mutableStateListOf(Group(user.id, "Personal", owner = user.id)) }
    var selectedGroup by remember {mutableStateOf(startingGroup ?: groups.first())}
    var isGroupExpanded by remember {mutableStateOf(false)}
    LaunchedEffect(Unit){
        startingGroup?.let{
            groups.add(it)
        }
        user.groups?.forEach { groupId ->
            try {
                supabase.from("groups")
                    .select {
                        filter {
                            eq("id", groupId)
                        }
                    }
                    .decodeSingleOrNull<Group>()?.let {
                        if (!groups.contains(it)) groups.add(it)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val dueDate = rememberDatePickerState()
    var showDatePicker by remember {mutableStateOf(false)}
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
                val assignment = supabase.from("assignments")
                    .insert(autoAssignment){
                        select()
                    }
                    .decodeSingle<Assignment>()
                //Update the group too (if not personal)
                supabase.from("groups")
                    .select {
                        filter {
                            eq("id", assignment.group_id)
                        }
                    }
                    .decodeSingleOrNull<Group>()?.let { group ->
                        supabase.from("groups")
                            .update(
                                {
                                    set("assignments", group.assignments + assignment.id)
                                }
                            ){
                                filter {
                                    eq("id", group.id)
                                }
                            }
                    }
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
        Spacer(Modifier.height(10.dp))
        TitleText("Add Assignment")
        InfoField(
            value = name,
            onValueChange = {name = it},
            labelText = "Name",
            isError = nameError,
            errorText = "Please enter an assignment name!"
        )
        Box(modifier = Modifier.fillMaxWidth()){
            InfoField(
                value = dueDate.selectedDateMillis?.let {
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(UTC).date.toString()
                } ?: "",
                onValueChange = {},
                readOnly = true,
                labelText = "Due Date",
                isError = false, //Maybe change this in the future
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable{showDatePicker = true}
            )
        }
        if (showDatePicker) DatePickerModal(
            onDismiss = {showDatePicker = false},
            title = "Due Date",
            onDateSelected = {
                dueDate.selectedDateMillis = it
                showDatePicker = false
            },
        )
        ExposedDropdownMenuBox(
            expanded = isGroupExpanded,
            onExpandedChange = { isGroupExpanded = !isGroupExpanded },
        ){
            InfoField(
                value = selectedGroup.name,
                onValueChange = {},
                readOnly = true,
                isError = false,
                labelText = "Group",
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isGroupExpanded,
                onDismissRequest = { isGroupExpanded = false },
                modifier = Modifier
                    .heightIn(max = 300.dp)
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
            capitalization = KeyboardCapitalization.Sentences
        )
        TertiaryButton(
            onClick = {
                submitAttempted = true
                if (!nameError) createAssignment()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
        ){
            Text(
                text = "Add Assignment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}