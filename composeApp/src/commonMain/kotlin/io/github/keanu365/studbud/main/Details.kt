package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.AlertType
import io.github.keanu365.studbud.AnimatedDropdown
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.DataView
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.SurfaceAlert
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import io.github.keanu365.studbud.theme.buttonColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus

@Composable
fun GroupDetailsPage(
    group: Group,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    onAssignmentClicked: (Assignment) -> Unit = {}
){
    val networkStatus by rememberNetworkStatus()
    var showMembers by remember { mutableStateOf(false) }
    val members = remember { mutableStateListOf<User>() }
    val assignments = remember { mutableStateListOf<Assignment>() }

    LaunchedEffect(group.members, networkStatus){
        try {
            members.clear()
            group.members.forEach { userId ->
                val user = supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<User>()
                members.add(user)
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
    LaunchedEffect(group.assignments, networkStatus){
        try {
            assignments.clear()
            group.assignments.forEach { assignmentId ->
                val assignment = supabase.from("assignments")
                    .select {
                        filter {
                            eq("id", assignmentId)
                        }
                    }
                    .decodeSingle<Assignment>()
                assignments.add(assignment)
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .padding(horizontal = 10.dp)
    ){
        Spacer(modifier = Modifier.height(10.dp))
        TitleText("Group Details")
        Text(
            text = "Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Group Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = group.id,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Description",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = group.description.ifEmpty { "No description provided." },
            style = MaterialTheme.typography.bodyLarge
        )
        if (networkStatus != NetworkStatus.Available){
            SurfaceAlert(
                alertType = AlertType.WARNING,
                message = "Your internet connection is unstable/lost! " +
                        "Group members and assignments will not appear properly until you connect to the internet."
            )
        }
        AnimatedDropdown(
            show = showMembers,
            title = "Members (${group.members.size})",
            dataList = members,
            onShowChanged = {showMembers = it}
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = "Assignments (${group.assignments.size})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Button(
                onClick = {
                    //TODO
                },
                colors = buttonColors()
            ){
                Text(
                    text = "+",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        DataView(
            dataList = assignments,
            onDataClicked = {onAssignmentClicked(it as Assignment)}
        )
    }
}

@Composable
fun AssignmentDetailsPage(
    assignment: Assignment,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    onGroupClicked: (Group) -> Unit
){
    var group by remember {mutableStateOf<Group?>(null)}
    val networkStatus by rememberNetworkStatus()
    LaunchedEffect(assignment.group_id, networkStatus){
        try {
            group = supabase.from("groups")
                .select {
                    filter {
                        eq("id", assignment.group_id)
                    }
                }
                .decodeSingleOrNull<Group>()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .padding(horizontal = 10.dp)
    ){
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Assignment Details",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 15.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = assignment.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Description",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = assignment.description.ifEmpty { "No description provided." },
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = group?.name ?: if (networkStatus == NetworkStatus.Available) "Personal" else "Connect to the internet to view the group!",
            style = MaterialTheme.typography.bodyLarge
        )
        group?.let{
            TertiaryButton(
                onClick = {
                    onGroupClicked(it)
                }
            ){
                Text(
                    text = "View Group Details",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = "Due on: ${assignment.due_date.day}/${assignment.due_date.month.number}/${assignment.due_date.year}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 5.dp)
        )
        val createdAt = remember {assignment.created_at.toLocalDateTime(TimeZone.currentSystemDefault())}
        val createDate = remember {"${createdAt.date.day}/${createdAt.date.month.number}/${createdAt.date.year}"}
        val createTime = remember {"${createdAt.time.hour}:${createdAt.time.minute}"}
        Text(
            text = "Created on $createDate at $createTime",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 5.dp, top = 10.dp)
        )
    }
}