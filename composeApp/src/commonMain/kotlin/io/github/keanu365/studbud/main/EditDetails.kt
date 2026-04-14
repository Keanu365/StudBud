package io.github.keanu365.studbud.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.AnimatedFAB
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.DataView
import io.github.keanu365.studbud.DatePickerModal
import io.github.keanu365.studbud.Divider
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.viewmodels.EditAssignmentViewModel
import io.github.keanu365.studbud.viewmodels.EditGroupViewModel
import kotlinx.datetime.number
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_check

@Composable
fun EditGroupPage(
    group: Group,
    onSave: (Group) -> Unit,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    viewModel: EditGroupViewModel = viewModel { EditGroupViewModel(group) }
){
    val networkStatus by rememberNetworkStatus()
    val newGroup by viewModel.newGroup.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val alert by viewModel.alert.collectAsStateWithLifecycle()

    alert()
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp)){
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Spacer(Modifier.height(10.dp))
            TitleText("Edit Group")
            Text("Please save your changes before returning.")
            Divider()
            InfoField(
                value = newGroup.name,
                onValueChange = {viewModel.setName(it)},
                labelText = "Name",
                isError = newGroup.name.isBlank(),
                errorText = "Please enter a group name!"
            )
            InfoField(
                value = newGroup.description,
                onValueChange = {viewModel.setDescription(it)},
                labelText = "Description",
                isError = false,
                singleLine = false
            )
            Divider()
            Text(
                text = "Members (Tap to Remove)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            DataView(
                dataList = members,
                firstLabel = "Name",
                onDataClicked = {
                    viewModel.showAlert(it as User)
                }
            )
            Divider()
            Text(
                text = "Assignments (Tap to Remove)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            DataView(
                dataList = assignments,
                firstLabel = "Name",
                secondLabel = "Due Date",
                onDataClicked = {
                    viewModel.showAlert(it as Assignment)
                }
            )
            Spacer(Modifier.height(100.dp))
        }
        AnimatedFAB(
            visible = group != newGroup
                    && networkStatus == NetworkStatus.Available
                    && newGroup.name.isNotBlank(),
            onClick = {
                viewModel.saveGroup()
                onSave(newGroup)
            },
            painter = painterResource(Res.drawable.icon_check),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 50.dp)
        )
    }
}

@Composable
fun EditAssignmentPage(
    assignment: Assignment,
    onSave: (Assignment) -> Unit,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    viewModel: EditAssignmentViewModel = viewModel { EditAssignmentViewModel(assignment) }
){
    val networkStatus by rememberNetworkStatus()
    val newAssignment by viewModel.newAssignment.collectAsStateWithLifecycle()
    val showDatePicker by viewModel.showDatePicker.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp)){
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Spacer(Modifier.height(10.dp))
            TitleText("Edit Assignment")
            Text("Please save your changes before returning.")
            Divider()
            InfoField(
                value = newAssignment.name,
                onValueChange = {viewModel.setName(it)},
                labelText = "Name",
                isError = newAssignment.name.isBlank(),
                errorText = "Please enter a group name!"
            )
            InfoField(
                value = newAssignment.description,
                onValueChange = {viewModel.setDescription(it)},
                labelText = "Description",
                isError = false,
                singleLine = false
            )
            Divider()
            Box(modifier = Modifier.fillMaxWidth()){
                InfoField(
                    value = "${newAssignment.due_date.day}/${newAssignment.due_date.month.number}/${newAssignment.due_date.year}",
                    onValueChange = {},
                    readOnly = true,
                    labelText = "Due Date",
                    isError = false,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable{viewModel.setShowDatePicker(true)}
                )
            }
            if (showDatePicker) DatePickerModal(
                onDismiss = {viewModel.setShowDatePicker(false)},
                title = "Due Date",
                onDateSelected = {
                    viewModel.setDueDate(it)
                },
            )
            Spacer(Modifier.height(100.dp))
        }
        AnimatedFAB(
            visible = assignment != newAssignment
                    && networkStatus == NetworkStatus.Available
                    && newAssignment.name.isNotBlank(),
            onClick = {
                viewModel.saveAssignment()
                onSave(newAssignment)
            },
            painter = painterResource(Res.drawable.icon_check),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 50.dp)
        )
    }
}