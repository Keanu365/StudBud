package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun Home(
    groups: List<Group>,
    onGroupClicked: (Group) -> Unit,
    assignments: List<Assignment>,
    onAssignmentClicked: (Assignment) -> Unit,
    onAddGroup: () -> Unit,
    showGroups: Boolean,
    showAssignments: Boolean,
    onShowGroup: (Boolean) -> Unit = {},
    onShowAssignments: (Boolean) -> Unit = {}
){
    val timeOfDay = remember {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time.hour
        when(hour) {
            in 0..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
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
            Text(
                text = "Good $timeOfDay!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 15.dp)
                    .fillMaxWidth()
            )
            AnimatedDropdown(
                show = showGroups,
                title = "My groups",
                secondLabel = "Members",
                dataList = groups,
                onShowChanged = {onShowGroup(it)},
                onDataClicked = {onGroupClicked(it as Group)}
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedDropdown(
                show = showAssignments,
                title = "My assignments",
                secondLabel = "Due Date",
                dataList = assignments,
                onShowChanged = {onShowAssignments(it)},
                onDataClicked = {onAssignmentClicked(it as Assignment)}
            )
        }
        Button(
            onClick = onAddGroup,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Join/Create a Group",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}