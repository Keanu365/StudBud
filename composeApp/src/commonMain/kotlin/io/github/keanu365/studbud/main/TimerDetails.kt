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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.*
import io.github.keanu365.studbud.viewmodels.TimerDetailsViewModel
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerDetails(
    assignments: List<Assignment>,
    onStart: (AutoUserAssignment) -> Unit,
    onStartSaved: (UserAssignment) -> Unit,
    startingAssignment: Assignment? = null,
    selectable: Boolean = startingAssignment == null,
    viewModel: TimerDetailsViewModel = viewModel { TimerDetailsViewModel(startingAssignment, onStartSaved) }
){
    val networkStatus by rememberNetworkStatus()
    val alert by viewModel.alert.collectAsStateWithLifecycle()
    val period by viewModel.period.collectAsStateWithLifecycle()
    val breaktime by viewModel.breaktime.collectAsStateWithLifecycle()
    val iterations by viewModel.iterations.collectAsStateWithLifecycle()
    val isPeriodError by viewModel.periodError.collectAsStateWithLifecycle()
    val isBreaktimeError by viewModel.breaktimeError.collectAsStateWithLifecycle()
    val isIterationsError by viewModel.iterationsError.collectAsStateWithLifecycle()
    val selectedAssignment by viewModel.selectedAssignment.collectAsStateWithLifecycle()
    val isAssignmentsExpanded by viewModel.assignmentsExpanded.collectAsStateWithLifecycle()

    LaunchedEffect(networkStatus){viewModel.getSavedAssignments()}

    alert()
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
            if (startingAssignment == null){
                if (networkStatus != NetworkStatus.Available){
                    SurfaceAlert(
                        alertType = AlertType.WARNING,
                        message = "While offline, no assignments will be available."
                    )
                    Spacer(Modifier.height(20.dp))
                } else {
                    TertiaryButton(
                        onClick = {viewModel.showSavedAssignments()}
                    ){
                        Text("View Saved Sessions")
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            ExposedDropdownMenuBox(
                expanded = isAssignmentsExpanded,
                onExpandedChange = { viewModel.toggleAssignments() },
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
                    onDismissRequest = { viewModel.toggleAssignments(false) },
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                ){
                    DropdownMenuItem(
                        text = {Text("None")},
                        onClick = {
                            viewModel.setSelectedAssignment(null)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    if (networkStatus == NetworkStatus.Available) assignments.forEach { assignment ->
                        DropdownMenuItem(
                            text = {Text(assignment.name)},
                            onClick = {
                                viewModel.setSelectedAssignment(assignment)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            InfoField(
                labelText = "Study Period",
                value = period,
                onValueChange = { viewModel.setPeriod(it) },
                isError = isPeriodError,
                errorText = "Period length is too short!",
                keyboardType = KeyboardType.Number
            )
            InfoField(
                labelText = "Break Time",
                value = breaktime,
                onValueChange = { viewModel.setBreaktime(it) },
                isError = isBreaktimeError,
                errorText = "Break time is too long!",
                keyboardType = KeyboardType.Number
            )
            InfoField(
                labelText = "Iterations",
                value = iterations,
                onValueChange = { viewModel.setIterations(it) },
                isError = isIterationsError,
                errorText = "Must be more than zero!",
                keyboardType = KeyboardType.Number
            )
        }
        TertiaryButton(
            onClick = {
                viewModel.checkDetails()?.let{ onStart(it) }
            },
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