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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_account
import studbud.composeapp.generated.resources.icon_home
import studbud.composeapp.generated.resources.icon_timer

@Composable
fun Homepage(
    onSignOut: () -> Unit,
    appPrefs: AppPreferences
){
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { Tabs.tabs.size })
    val currentTab = Tabs.tabs[pagerState.currentPage]
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit){
        pagerState.scrollToPage(1)
        user = supabase.from("profiles")
            .select {
                filter { eq("id", appPrefs.userId.first()) }
            }
            .decodeSingleOrNull<User>()
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
                    Tabs.TIMER -> Timer()
                    Tabs.HOME -> Home(
                        user = user,
                        onAddGroup = {
                            //TODO
                        }
                    )
                    Tabs.PROFILE -> Profile(
                        onSignOut = onSignOut,
                        user = user
                    )
                }
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