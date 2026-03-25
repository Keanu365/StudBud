package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoUserAssignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_account
import studbud.composeapp.generated.resources.icon_home
import studbud.composeapp.generated.resources.icon_timer

@Composable
fun Homepage(
    onSignOut: () -> Unit,
    appPrefs: AppPreferences,
    showSnackBar: (String) -> Unit,
    onUserLoaded: (User) -> Unit = {},
    onAddGroup: (User) -> Unit,
    onGroupClicked: (Group) -> Unit,
    onAssignmentClicked: (Assignment) -> Unit,
    onAssignmentAdd: (User) -> Unit,
    onTimerStart: (AutoUserAssignment?) -> Unit
){
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by rememberNetworkStatus()
    val pagerState = rememberPagerState(pageCount = { Tabs.tabs.size })
    val currentTab = Tabs.tabs[pagerState.currentPage]

    var user by remember { mutableStateOf<User?>(null) }
    val groups = remember { mutableStateListOf<Group>() }
    val assignments = remember {mutableStateListOf<Assignment>()}

    var showGroups by rememberSaveable { mutableStateOf(false) }
    var showAssignments by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit){
        pagerState.scrollToPage(1)
    }
    LaunchedEffect(networkStatus){
        groups.clear()
        assignments.clear()
        if (networkStatus == NetworkStatus.Available){
            //User
            user = supabase.from("profiles")
                .select {
                    filter { eq("id", appPrefs.userId.first()) }
                }
                .decodeSingleOrNull<User>()
            user?.let{onUserLoaded(it)}
            //Groups
            val currentGroups: List<Group>? = user?.let {
                val userGroups = it.groups
                val groupList = mutableListOf<Group>()
                userGroups?.forEach { groupId ->
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
            //Assignments
            groups.forEach { group ->
                group.assignments.forEach { assignmentId ->
                    val assignment = supabase.from("assignments")
                        .select {
                            filter {
                                eq("id", assignmentId)
                            }
                        }
                        .decodeSingleOrNull<Assignment>()
                    if (assignment != null) assignments.add(assignment)
                }
            }
            user?.let{
                supabase.from("assignments")
                    .select{
                        filter{
                            eq("group_id", it.id)
                        }
                    }
                    .decodeList<Assignment>()
                    .forEach{ assignment ->
                        assignments.add(assignment)
                    }
            }
            //And finally store it locally in case user is offline the next time round
            appPrefs.saveRawData(user, groups, assignments)
            println("All successful!")
        } else {
            user = Json.decodeFromString(appPrefs.rawUserData.first())
            groups.addAll(Json.decodeFromString(appPrefs.rawGroupsData.first()))
            assignments.addAll(Json.decodeFromString(appPrefs.rawAssignmentsData.first()))
        }
    }

    fun tryAndCatch(block: () -> Unit){
        if (networkStatus != NetworkStatus.Available){
            showSnackBar("Please connect to the Internet!")
        } else {
            try {
                block()
            } catch (_: NullPointerException) {
                showSnackBar("Error fetching user data. Please wait a moment before trying again.")
            } catch (_: Exception) {
                showSnackBar("Something went wrong. Please try again later.")
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(100.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                TabItem(
                    text = "Timer",
                    selected = currentTab == Tabs.TIMER,
                    icon = Res.drawable.icon_timer
                ){
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
                Spacer(Modifier.width(50.dp))
                TabItem(
                    text = "Home",
                    selected = currentTab == Tabs.HOME,
                    icon = Res.drawable.icon_home
                ){
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
                Spacer(Modifier.width(50.dp))
                TabItem(
                    text = "Profile",
                    selected = currentTab == Tabs.PROFILE,
                    icon = Res.drawable.icon_account
                ){
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            }
        }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ){
                when(Tabs.tabs[it]){
                    Tabs.TIMER -> TimerDetails(
                        assignments = assignments,
                        onStart = { assignment ->
                            tryAndCatch { onTimerStart(assignment) }
                        }
                    )
                    Tabs.HOME -> Home(
                        onAddGroup = {
                            tryAndCatch { onAddGroup(user!!) }
                        },
                        groups = groups,
                        assignments = assignments,
                        showGroups = showGroups,
                        showAssignments = showAssignments,
                        onShowGroup = {show -> showGroups = show},
                        onShowAssignments = {show -> showAssignments = show},
                        onGroupClicked = onGroupClicked,
                        onAssignmentClicked = onAssignmentClicked,
                        onAssignmentAdd = {
                            tryAndCatch { onAssignmentAdd(user!!) }
                        }
                    )
                    Tabs.PROFILE -> Profile(
                        onSignOut = {
                            tryAndCatch { onSignOut() }
                        },
                        user = user
                    )
                }
                Spacer(Modifier.height(60.dp)) //Buffer for buttons
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    icon: DrawableResource,
    onClick: () -> Unit
){
    NavigationBarItem(
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
            selectedTextColor = MaterialTheme.colorScheme.onSecondary,
            unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
            unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
            indicatorColor = MaterialTheme.colorScheme.secondary
        ),
        icon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onSecondary
                else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .size(48.dp)
            )
        },
        label = {Text(text)},
        selected = selected,
        onClick = onClick,
    )
}

private enum class Tabs{
    TIMER, HOME, PROFILE;
    companion object {
        val tabs = listOf(TIMER, HOME, PROFILE)
    }
}