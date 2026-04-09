package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.keanu365.studbud.Achievement
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoUserAssignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.getDeviceType
import io.github.keanu365.studbud.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_account
import studbud.composeapp.generated.resources.icon_home
import studbud.composeapp.generated.resources.icon_refresh
import studbud.composeapp.generated.resources.icon_timer

@Composable
fun Homepage(
    onSignOut: () -> Unit,
    viewModel: MainViewModel,
    showSnackBar: (String) -> Unit,
    onAddGroup: () -> Unit,
    onGroupClicked: (Group) -> Unit,
    onAssignmentClicked: (Assignment) -> Unit,
    onAssignmentAdd: () -> Unit,
    onTimerStart: (AutoUserAssignment) -> Unit,
    onViewPhoto: () -> Unit = {},
    onEditPhoto: () -> Unit = {},
){
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by rememberNetworkStatus()
    val pagerState = rememberPagerState(pageCount = { Tabs.tabs.size })
    val currentTab = Tabs.tabs[pagerState.currentPage]

    val user by viewModel.user.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val newAchievements = remember { mutableStateListOf<Achievement>() }

    var showGroups by rememberSaveable { mutableStateOf(false) }
    var showAssignments by rememberSaveable { mutableStateOf(false) }

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

    var isRefreshing by remember {mutableStateOf(false)}
    var onLaunchRefresh by remember {mutableStateOf(true)}
    var hasRefreshedThisSession by rememberSaveable { mutableStateOf(false) }
    fun refresh() {
        if (networkStatus != NetworkStatus.Available) {
            if (onLaunchRefresh) onLaunchRefresh = false
            else showSnackBar("Please connect to the Internet!")
            return
        }
        coroutineScope.launch {
            try {
                isRefreshing = true
                newAchievements.clear() //Just in case
                newAchievements.addAll(viewModel.refreshUser())
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBar("Something went wrong with the refresh. Please try again later.")
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit){
        pagerState.scrollToPage(1)
        if (!hasRefreshedThisSession && networkStatus == NetworkStatus.Available) {
            refresh()
            hasRefreshedThisSession = true
        }
    }
    LaunchedEffect(networkStatus){
        if (networkStatus == NetworkStatus.Available && !hasRefreshedThisSession && !isRefreshing) {
            refresh()
            hasRefreshedThisSession = true
        }
    }

    newAchievements.forEach { newAchievement ->
        AlertDialog(
            onDismissRequest = {
                newAchievements.remove(newAchievement)
            },
            confirmButton = {
                TertiaryButton(
                    onClick = {
                        newAchievements.remove(newAchievement)
                    }
                ){Text("OK")}
            },
            title = { TitleText("Achievement Earned!") },
            text = {
                Column {
                    AsyncImage(
                        model = newAchievement.badge_url,
                        // Note: The image does not load if dialogs are stacked.
                        // TODO fix image loading
                        contentDescription = null
                    )
                    Text(
                        text = newAchievement.name,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth()
                    )
                    Text(
                        text = "Go to the Achievements tab to view more details!"
                    )
                }
            }
        )
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {refresh()},
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
                            onTimerStart(assignment)
                        }
                    )
                    Tabs.HOME -> Home(
                        onAddGroup = {
                            tryAndCatch { onAddGroup() }
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
                            tryAndCatch { onAssignmentAdd() }
                        }
                    )
                    Tabs.PROFILE -> Profile(
                        onSignOut = {
                            tryAndCatch { onSignOut() }
                        },
                        user = user,
                        onViewPhoto = { tryAndCatch { onViewPhoto() } },
                        onEditPhoto = { tryAndCatch { onEditPhoto() } }
                    )
                }
                Spacer(Modifier.height(60.dp)) //Buffer for buttons
            }
            if (getDeviceType() == "Desktop") IconButton(
                enabled = !isRefreshing,
                onClick = {refresh()},
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 15.dp)
                    .size(50.dp)
            ){
                Icon(
                    painter = painterResource(Res.drawable.icon_refresh),
                    contentDescription = "Refresh",
                )
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