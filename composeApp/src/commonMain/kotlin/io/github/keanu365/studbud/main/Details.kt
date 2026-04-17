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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.AlertType
import io.github.keanu365.studbud.AnimatedDropdown
import io.github.keanu365.studbud.AnimatedFAB
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AssignmentsDropdown
import io.github.keanu365.studbud.Divider
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.SurfaceAlert
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.viewmodels.AssignmentDetailsViewModel
import io.github.keanu365.studbud.viewmodels.GroupDetailsViewModel
import kotlinx.datetime.number
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_delete
import studbud.composeapp.generated.resources.icon_edit
import studbud.composeapp.generated.resources.icon_exit

@Composable
fun GroupDetailsPage(
    group: Group,
    user: User? = null,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    showActions: Boolean = true,
    onAssignmentClicked: (Assignment) -> Unit = {},
    onAssignmentAdd: () -> Unit = {},
    onEdit: () -> Unit = {},
    onFinish: (String) -> Unit = {},
    viewModel: GroupDetailsViewModel = viewModel { GroupDetailsViewModel(group, user, onFinish) },
){
    val networkStatus by rememberNetworkStatus()
    var showMembers by remember { mutableStateOf(false) }
    var showAssignments by remember { mutableStateOf(false) }
    val members by viewModel.members.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val alert by viewModel.alert.collectAsStateWithLifecycle()

    LaunchedEffect(networkStatus){ viewModel.refresh() }

    alert()
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier
                .padding(horizontal = 10.dp)
        ){
            Spacer(modifier = Modifier.height(10.dp))
            TitleText("Group Details")
            Divider()
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
            Divider()
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
            Divider()
            if (networkStatus != NetworkStatus.Available){
                SurfaceAlert(
                    alertType = AlertType.WARNING,
                    message = "Group members and assignments may not appear until you connect to the internet."
                )
            }
            if (showActions) AnimatedDropdown(
                show = showMembers,
                title = "Members (${group.members.size})",
                dataList = members,
                onShowChanged = {showMembers = it}
            )
            if (showActions) AssignmentsDropdown(
                show = showAssignments,
                onShowChanged = {showAssignments = it},
                title = "Assignments (${group.assignments.size})",
                assignments = assignments,
                onAssignmentClicked = onAssignmentClicked,
                onAssignmentAdd = onAssignmentAdd
            )
            Spacer(Modifier.height(100.dp))
        }
        if (showActions) user?.let {
            val isOwner = it.id == group.owner
            AnimatedFAB(
                visible = networkStatus == NetworkStatus.Available,
                onClick = {
                    if (isOwner) onEdit()
                    else { viewModel.showLeaveAlert() }
                },
                painter = painterResource(
                    if (isOwner) Res.drawable.icon_edit
                    else Res.drawable.icon_exit
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 50.dp)

            )
            if (isOwner) AnimatedFAB(
                visible = networkStatus == NetworkStatus.Available,
                onClick = {
                    viewModel.showDeleteAlert()
                },
                error = true,
                painter = painterResource(Res.drawable.icon_delete),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 50.dp)
            )
        }
    }
}

@Composable
fun AssignmentDetailsPage(
    assignment: Assignment,
    modifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    user: User? = null,
    showActions: Boolean = true,
    onGroupClicked: (Group) -> Unit = {},
    onDo: ((Assignment) -> Unit)? = null,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    viewModel: AssignmentDetailsViewModel = viewModel { AssignmentDetailsViewModel(assignment, onDelete) },
){
    val group by viewModel.group.collectAsStateWithLifecycle()
    val networkStatus by rememberNetworkStatus()
    LaunchedEffect(networkStatus){ viewModel.getGroup() }
    val alert by viewModel.alert.collectAsStateWithLifecycle()

    alert()
    Box(modifier = Modifier.fillMaxSize()){
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
            Divider()
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
            Divider()
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
            Divider()
            Text(
                text = "Group",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = group?.name
                    ?: if (networkStatus == NetworkStatus.Available) "Personal"
                    else "Connect to the internet to view the group!"
            )
            if (showActions) group?.let{
                Text(
                    text = "View Group",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clickable{onGroupClicked(it)}
                )
            }
            Divider()
            Text(
                text = "Due on: ${assignment.due_date.day}/${assignment.due_date.month.number}/${assignment.due_date.year}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Text(
                text = "Created on ${viewModel.createDate} at ${viewModel.createTime}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 5.dp, top = 10.dp)
            )
            Spacer(Modifier.height(20.dp))
            onDo?.let{
                if (networkStatus == NetworkStatus.Available) TertiaryButton(
                    onClick = { it(assignment) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                ){
                    Text(
                        text = "Do This Assignment",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(Modifier.height(100.dp))
        }
        if (showActions && (user?.id == group?.owner || user?.id == assignment.group_id)) {
            AnimatedFAB(
                visible = networkStatus == NetworkStatus.Available,
                onClick = onEdit,
                painter = painterResource(Res.drawable.icon_edit),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 50.dp)
            )
            AnimatedFAB(
                visible = networkStatus == NetworkStatus.Available,
                onClick = {
                    viewModel.showDeleteAlert()
                },
                error = true,
                painter = painterResource(Res.drawable.icon_delete),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 50.dp)
            )
        }
    }
}