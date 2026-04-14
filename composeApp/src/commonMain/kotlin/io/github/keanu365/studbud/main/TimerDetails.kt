package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.*
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerDetails(
    assignments: List<Assignment>,
    onStart: (AutoUserAssignment) -> Unit,
    startingAssignment: Assignment? = null,
    selectable: Boolean = startingAssignment == null
){
    val networkStatus by rememberNetworkStatus()
    //TODO Add a way for user to access "saved" assignments (i.e. incomplete)
    var period by remember { mutableStateOf("25") }
    var breaktime by remember { mutableStateOf("5") }
    var iterations by remember { mutableStateOf("1") }

    var isPeriodError by remember {mutableStateOf(false)}
    var isBreaktimeError by remember {mutableStateOf(false)}
    var isIterationsError by remember {mutableStateOf(false)}

    var isAssignmentsExpanded by remember { mutableStateOf(false) }
    var selectedAssignment by remember {mutableStateOf(startingAssignment)}
    fun checkDetails() = run {
        val period = period.ifEmpty {
            period = "0"
            "0"
        }.filter{it.isDigit()}.toInt()
        val breaktime = breaktime.ifEmpty {
            breaktime = "0"
            "0"
        }.filter{it.isDigit()}.toInt()
        val iterations = iterations.ifEmpty {
            iterations = "0"
            "0"
        }.filter{it.isDigit()}.toInt()

        isPeriodError = period <= 0 || period <= breaktime + 5
        isBreaktimeError = breaktime !in 1..period-5
        isIterationsError = iterations <= 0
        if (!isPeriodError && !isBreaktimeError && !isIterationsError){
            onStart(AutoUserAssignment(
                assignment_id = selectedAssignment?.id ?: "",
                period = period,
                breaktime = breaktime,
                iterations = iterations
            ))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .verticalScroll(rememberScrollState())
        ){
            TitleText("Study Timer")
            if (networkStatus != NetworkStatus.Available){
                SurfaceAlert(
                    alertType = AlertType.WARNING,
                    message = "While offline, no assignments will be available."
                )
                Spacer(Modifier.height(20.dp))
            }
            ExposedDropdownMenuBox(
                expanded = isAssignmentsExpanded,
                onExpandedChange = { isAssignmentsExpanded = !isAssignmentsExpanded },
            ){
                InfoField(
                    value = selectedAssignment?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    isError = false,
                    labelText = "Assignment",
                    trailingIcon = if (selectable) {
                        {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAssignmentsExpanded)}
                    } else null,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                if (selectable) ExposedDropdownMenu(
                    expanded = isAssignmentsExpanded,
                    onDismissRequest = { isAssignmentsExpanded = false },
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                ){
                    DropdownMenuItem(
                        text = {Text("None")},
                        onClick = {
                            selectedAssignment = null
                            isAssignmentsExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    if (networkStatus == NetworkStatus.Available) assignments.forEach { assignment ->
                        DropdownMenuItem(
                            text = {Text(assignment.name)},
                            onClick = {
                                selectedAssignment = assignment
                                isAssignmentsExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            InfoField(
                labelText = "Study Period",
                value = period,
                onValueChange = { period = it },
                isError = isPeriodError,
                errorText = "Period length is too short!",
                keyboardType = KeyboardType.Number
            )
            InfoField(
                labelText = "Break Time",
                value = breaktime,
                onValueChange = { breaktime = it },
                isError = isBreaktimeError,
                errorText = "Break time is too long!",
                keyboardType = KeyboardType.Number
            )
            InfoField(
                labelText = "Iterations",
                value = iterations,
                onValueChange = { iterations = it },
                isError = isIterationsError,
                errorText = "Must be more than zero!",
                keyboardType = KeyboardType.Number
            )
        }
        TertiaryButton(
            onClick = { checkDetails() },
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Start Timer",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}