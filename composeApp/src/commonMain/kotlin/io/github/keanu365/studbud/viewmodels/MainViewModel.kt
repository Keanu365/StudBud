package io.github.keanu365.studbud.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Achievement
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoUserAssignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.navigation.Route
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class MainViewModel(
    val appPrefs: AppPreferences
): ViewModel() {
    @Composable
    fun rememberBackStack() = rememberNavBackStack(
        configuration = SavedStateConfiguration{
            serializersModule = SerializersModule{
                polymorphic(NavKey::class){
                    subclass(Route.SplashScreen::class, Route.SplashScreen.serializer())
                    subclass(Route.ThemeTest::class, Route.ThemeTest.serializer())
                    subclass(Route.SignUpPage::class, Route.SignUpPage.serializer())
                    subclass(Route.SignInPage::class, Route.SignInPage.serializer())
                    subclass(Route.Homepage::class, Route.Homepage.serializer())
                    subclass(Route.AddGroupPage::class, Route.AddGroupPage.serializer())
                    subclass(Route.GroupDetailsPage::class, Route.GroupDetailsPage.serializer())
                    subclass(Route.AssignmentDetailsPage::class, Route.AssignmentDetailsPage.serializer())
                    subclass(Route.SuccessPage::class, Route.SuccessPage.serializer())
                    subclass(Route.AddAssignmentPage::class, Route.AddAssignmentPage.serializer())
                    subclass(Route.TimerPage::class, Route.TimerPage.serializer())
                    subclass(Route.TimerDetailsPage::class, Route.TimerDetailsPage.serializer())
                    subclass(Route.SettingsPage::class, Route.SettingsPage.serializer())
                    subclass(Route.ImageViewPage::class, Route.ImageViewPage.serializer())
                    subclass(Route.Leaderboard::class, Route.Leaderboard.serializer())
                    subclass(Route.AchievementsPage::class, Route.AchievementsPage.serializer())
                }
            }
        },
        Route.SplashScreen
    )

    //User Information
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups = _groups.asStateFlow()
    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments = _assignments.asStateFlow()
    private val _allAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val allAchievements = _allAchievements.asStateFlow()

    fun signIn(newUser: User){
        viewModelScope.launch {
            appPrefs.saveSignIn(newUser.id)
            appPrefs.setFirstTimeUser(false)
            awardAchievement(0, 1) // Welcome achievement
        }
    }
    fun signOut(){
        viewModelScope.launch {
            supabase.auth.signOut()
            appPrefs.signOut()
            _user.value = null
        }
    }

    private var isRefreshing = false
    private val _newAchievementEvents = kotlinx.coroutines.flow.MutableSharedFlow<List<Achievement>>()
    val newAchievementEvents = _newAchievementEvents.asSharedFlow() // Use asSharedFlow if preferred// 2. Fix refreshUser to use 'finally' so the flag always resets

    suspend fun refreshUser(): List<Achievement> {
        if (isRefreshing) return emptyList()
        isRefreshing = true
        try {
            val user = supabase.from("profiles")
                .select {
                    filter {
                        eq(
                            "id",
                        supabase.auth.currentUserOrNull()?.id ?: appPrefs.userId.first()
                        )
                    }
                }
                .decodeSingle<User>()

            val newOnes = setUser(user)

            // 3. BROADCAST the new achievements!
            if (newOnes.isNotEmpty()) {
                _newAchievementEvents.emit(newOnes)
            }

            return newOnes
        } catch (_: NullPointerException) {
            println("Error loading user data. Please try again.")
        } catch (_: HttpRequestException){
            println("Error loading data. Please ensure you are connected to the Internet.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Something went wrong. Please try again.")
        } finally {
            isRefreshing = false
        }
        return emptyList()
    }
    private suspend fun setUser(newUser: User): List<Achievement> {
        //Groups
        val userGroups = newUser.groups
        val newGroups = mutableListOf<Group>()
        userGroups?.forEach { groupId ->
            supabase.from("groups")
                .select {
                    filter {
                        eq("id", groupId)
                    }
                }
                .decodeSingleOrNull<Group>()?.let{
                    newGroups.add(it)
                }
        }
        _groups.emit(newGroups)
        //Assignments
        val newAssignments = mutableListOf<Assignment>()
        newGroups.forEach { group ->
            group.assignments.forEach { assignmentId ->
                supabase.from("assignments")
                    .select {
                        filter {
                            eq("id", assignmentId)
                        }
                    }
                    .decodeSingleOrNull<Assignment>()?.let{
                        newAssignments.add(it)
                    }
            }
        }
        supabase.from("assignments")
            .select{
                filter{
                    eq("group_id", newUser.id)
                }
            }
            .decodeList<Assignment>()
            .forEach{ assignment ->
                newAssignments.add(assignment)
            }
        _assignments.emit(newAssignments)
        //Check for new achievements
        val rawData = appPrefs.rawUserData.first()
        val oldAchievements = _user.value?.achievements
            ?: if (rawData.isNotEmpty() && rawData != "null") {
                try {
                    Json.decodeFromString<User?>(rawData)?.achievements
                } catch (_: Exception) { null }
            } else {
                null
            } ?: emptyList()
        val allAchievements = supabase.from("achievements")
            .select()
            .decodeList<Achievement>()
        _allAchievements.emit(allAchievements)
        val newAchievements = mutableListOf<Achievement>()
        newUser.achievements?.forEach { achId ->
            val isNew = !oldAchievements.contains(achId)
            if (isNew) {
                allAchievements.find { it.id == achId }?.let {
                    newAchievements.add(it)
                }
            }
        }
        //Set user last to comply with achievements logic
        _user.value = newUser
        //And then save everything to the device
        appPrefs.saveRawUserData(newUser, newGroups, newAssignments, allAchievements)
        println("New achievements: $newAchievements")
        return newAchievements
    }

    suspend fun awardAchievement(vararg ids: Int, refresh: Boolean = true){
        for (id in ids) {
            if (_user.value?.achievements?.contains(id) ?: true) continue
            supabase.from("achievements")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Achievement>()
            supabase.from("profiles")
                .update (
                    { set("achievements", _user.value!!.achievements?.plus(id) ?: listOf(id)) }
                ) {
                    filter {
                        eq("id", _user.value!!.id)
                    }
                }
        }
        if (refresh) refreshUser()
    }

    //"In Focus" stuff
    val _groupInFocus = MutableStateFlow<Group?>(null)
    val groupInFocus = _groupInFocus.asStateFlow()
    val setGroupInFocus = {group: Group -> _groupInFocus.value = group}
    val _assignmentInFocus = MutableStateFlow<Assignment?>(null)
    val assignmentInFocus = _assignmentInFocus.asStateFlow()
    val setAssignmentInFocus = {assignment: Assignment -> _assignmentInFocus.value = assignment}
    val _userAssignmentInFocus = MutableStateFlow<UserAssignment?>(null)
    val userAssignmentInFocus = _userAssignmentInFocus.asStateFlow()

    fun prepareTimer(assignment: AutoUserAssignment){
        viewModelScope.launch {
            _user.value?.let{user ->
                _userAssignmentInFocus.value = if (assignment.assignment_id.isBlank())
                    UserAssignment(
                        user_id = user.id,
                        period = assignment.period,
                        breaktime = assignment.breaktime,
                        iterations = assignment.iterations
                    )
                else supabase.from("user_assignments")
                    .insert(assignment){select()}
                    .decodeSingle<UserAssignment>()
            } ?: println("Timer cannot be started as user is null.")
        }
    }
    fun endTimer(userAssignment: UserAssignment, studsToAdd: Int = 0){
        viewModelScope.launch {
            try {
                if (userAssignment.assignment_id.isNotEmpty()) {
                    supabase.from("user_assignments")
                        .update(
                            {
                                set("completed", true)
                            }
                        ) {
                            filter {
                                eq("assignment_id", userAssignment.assignment_id)
                                eq("user_id", userAssignment.user_id)
                            }
                        }
                }
                _user.value = supabase.from("profiles")
                    .update(
                        {
                            set("studs", _user.value!!.studs + studsToAdd)
                            set("all_time_studs", _user.value!!.all_time_studs + studsToAdd)
                        }
                    ){
                        filter {
                            eq("id", _user.value!!.id)
                        }
                        select()
                    }
                    .decodeSingle<User>()
                awardAchievement(4, refresh = false)
            } catch (_: HttpRequestException) {
                println("Failed to save studs to database. Studs will be added the next time you're connected to the internet.")
            } catch (_: NullPointerException) {
                println("User could not be found. Studs will be added the next time you're connected to the internet.")
            }
        }
    }

    //Indicate here if you want back arrow / actions / top bar
    val showBackKeys = listOf(
        Route.AddGroupPage,
        Route.GroupDetailsPage,
        Route.AssignmentDetailsPage,
        Route.AddAssignmentPage,
        Route.TimerDetailsPage,
        Route.SettingsPage,
        Route.Leaderboard,
        Route.AchievementsPage
    )
    val showActionsKeys = listOf(
        Route.ThemeTest,
        Route.Homepage,
        Route.AddGroupPage,
        Route.GroupDetailsPage,
        Route.AssignmentDetailsPage,
        Route.TimerDetailsPage
    )
    val hideTopBar = listOf(
        Route.SplashScreen,
        Route.ImageViewPage
    )

    init {
        viewModelScope.launch {
            try {
                val userRaw = appPrefs.rawUserData.first()
                if (userRaw.isNotEmpty() && userRaw != "null") {
                    _user.value = Json.decodeFromString(userRaw)
                    _groups.value = Json.decodeFromString(appPrefs.rawGroupsData.first())
                    _assignments.value = Json.decodeFromString(appPrefs.rawAssignmentsData.first())
                    _allAchievements.value = Json.decodeFromString(appPrefs.rawAllAchievementsData.first())
                } else {
                    // Only refresh if we actually have NO data
                    refreshUser()
                }
            } catch (_: Exception) {
                // Data was corrupt, refresh once
                refreshUser()
            }
        }
    }
}