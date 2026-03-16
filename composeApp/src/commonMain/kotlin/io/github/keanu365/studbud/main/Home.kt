package io.github.keanu365.studbud.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_arrow_dropdown
import kotlin.time.Clock

@Composable
fun Home(
    user: User?,
    onAddGroup: () -> Unit //Might want to add the user as a parameter
){
    val groups = remember { mutableStateListOf<Group>() }
    var showGroups by remember {mutableStateOf(false)}
    var showAssignments by remember {mutableStateOf(false)}
    val timeOfDay = remember {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time.hour
        when(hour) {
            in 0..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
        }
    }

    LaunchedEffect(user){
        val currentGroups: List<Group>? = user?.let{
            val userGroups = it.groups
            val groupList = mutableListOf<Group>()
            userGroups?.forEach{ groupId ->
                val group = supabase.from("groups")
                    .select {
                        filter {
                            eq("id", groupId)
                        }
                    }
                    .decodeSingleOrNull<Group>()
                if (group != null) groupList.add(group)
            }
            groupList
        }
        groups.addAll(currentGroups ?: emptyList())
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "My groups",
                    style = MaterialTheme.typography.labelLarge
                )
                IconButton(
                    onClick = {showGroups = !showGroups},
                    modifier = Modifier
                ){
                    Icon(
                        painter = painterResource(Res.drawable.icon_arrow_dropdown),
                        contentDescription = null,
                        modifier = animateDropdown(showGroups)
                    )
                }
            }
            AnimatedVisibility(showGroups){
                Spacer(modifier = Modifier.height(10.dp))
                var counter = 0
                Column{
                    groups.forEach { group ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (counter % 2 == 0) Color.Transparent
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        ){
                            Text(
                                text = group.name,
                                fontSize = 16.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .fillMaxWidth(0.55f)
                                    .horizontalScroll(rememberScrollState()) //In case name is too long
                            )
                            Text(
                                text = "${group.members.size} member${if (group.members.size != 1) "s" else ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        counter++
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "My assignments",
                    style = MaterialTheme.typography.labelLarge
                )
                IconButton(
                    onClick = {showAssignments = !showAssignments},
                    modifier = Modifier
                ){
                    Icon(
                        painter = painterResource(Res.drawable.icon_arrow_dropdown),
                        contentDescription = null,
                        modifier = animateDropdown(showAssignments)
                    )
                }
            }
            AnimatedVisibility(showAssignments){
                //Placeholder
                Spacer(modifier = Modifier.height(50.dp))
            }
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