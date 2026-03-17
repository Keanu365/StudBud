package io.github.keanu365.studbud.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_arrow_dropdown
import kotlin.time.Clock

@Composable
fun Home(
    groups: List<Group>,
    assignments: List<Assignment>,
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
                onShowChanged = {onShowGroup(it)}
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedDropdown(
                show = showAssignments,
                title = "My assignments",
                secondLabel = "Due Date",
                dataList = assignments,
                onShowChanged = {onShowAssignments(it)}
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

@Composable
private fun AnimatedDropdown(
    show: Boolean,
    title: String,
    firstLabel: String = "Name",
    secondLabel: String = " ",
    dataList: List<Any>,
    onShowChanged: (Boolean) -> Unit = {}
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
        IconButton(
            onClick = {
                onShowChanged(!show)
            },
            modifier = Modifier
        ){
            Icon(
                painter = painterResource(Res.drawable.icon_arrow_dropdown),
                contentDescription = null,
                modifier = animateDropdown(show)
            )
        }
    }
    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(initialOffsetY = { -40 }) + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(initialAlpha = 0.3f),
        exit = slideOutVertically(targetOffsetY = { -40 }) + shrinkVertically(
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ){
        Column(Modifier.padding(vertical = 10.dp)){
            if (dataList.isEmpty()) Text(
                text = "Nothing to see here yet!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) else Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 5.dp)
            ){
                Text(
                    text = firstLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = secondLabel,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            dataList.forEachIndexed { index, data ->
                var firstText = ""
                var secondText = ""
                if (data is Group) {
                    firstText = data.name
                    secondText = "${data.members.size}"
                } else if (data is Assignment){
                    firstText = data.name
                    secondText = "${data.due_date.day}/${data.due_date.month.number}/${data.due_date.year}"
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (index % 2 == 0) Color.Transparent
                            else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(5.dp)
                        )
                ){
                    Text(
                        text = firstText,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .fillMaxWidth(0.6f)
                            .horizontalScroll(rememberScrollState()) //In case name is too long
                    )
                    Text(
                        text = secondText,
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(horizontal = 5.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun animateDropdown(
    show: Boolean
): Modifier {
    return Modifier.rotate(
        animateFloatAsState(
            targetValue = if (show) 180f else 0f,
            label = "Dropdown Animation"
        ).value
    )
}
