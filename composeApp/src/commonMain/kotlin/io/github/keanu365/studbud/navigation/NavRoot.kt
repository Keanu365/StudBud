package io.github.keanu365.studbud.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.ismoy.imagepickerkmp.domain.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.domain.models.MimeType
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.keanu365.studbud.SplashLength
import io.github.keanu365.studbud.SplashScreen
import io.github.keanu365.studbud.SuccessPage
import io.github.keanu365.studbud.ThemeTest
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.account.SignInPage
import io.github.keanu365.studbud.account.SignUpPage
import io.github.keanu365.studbud.account.isEmailValid
import io.github.keanu365.studbud.main.AchievementsPage
import io.github.keanu365.studbud.main.AddAssignmentPage
import io.github.keanu365.studbud.main.AddGroupPage
import io.github.keanu365.studbud.main.AssignmentDetailsPage
import io.github.keanu365.studbud.main.EditAssignmentPage
import io.github.keanu365.studbud.main.EditGroupPage
import io.github.keanu365.studbud.main.GroupDetailsPage
import io.github.keanu365.studbud.main.Homepage
import io.github.keanu365.studbud.main.ImageView
import io.github.keanu365.studbud.main.Leaderboard
import io.github.keanu365.studbud.main.SettingsPage
import io.github.keanu365.studbud.main.Timer
import io.github.keanu365.studbud.main.TimerDetails
import io.github.keanu365.studbud.supabase
import io.github.keanu365.studbud.viewmodels.MainViewModel
import io.github.keanu365.studbud.viewmodels.SettingsViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.congrats
import studbud.composeapp.generated.resources.success
import kotlin.time.Clock

@Composable
fun NavRoot(
    viewModel: MainViewModel,
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(appPrefs = viewModel.appPrefs) },
    showSnackBar: (String) -> Unit,
){
    val appPrefs = viewModel.appPrefs
    
    val coroutineScope = rememberCoroutineScope()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val groupInFocus by viewModel.groupInFocus.collectAsStateWithLifecycle()
    val assignmentInFocus by viewModel.assignmentInFocus.collectAsStateWithLifecycle()
    val userAssignmentInFocus by viewModel.userAssignmentInFocus.collectAsStateWithLifecycle()
    fun refresh() { coroutineScope.launch { viewModel.refreshUser() } }

    var sharedEmail by remember {mutableStateOf("")}
    var sharedUsername by remember {mutableStateOf("")}
    var sharedPassword by remember {mutableStateOf("")}

    var splashLength by remember {mutableStateOf(SplashLength.MEDIUM)}
    fun onSignIn(user: User, key: NavKey) = run {
        viewModel.signIn(user)
        sharedEmail = ""
        sharedUsername = ""
        sharedPassword = ""
        showSnackBar("Signed in successfully!")
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }
    fun onSignOut(key: NavKey) = run {
        viewModel.signOut()
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }
    fun startTimer(assignment: Any){
        try {
            coroutineScope.launch{
                viewModel.prepareTimer(assignment)
                backStack.add(Route.TimerPage)
                for (i in backStack.size - 2 downTo 0) {
                    if (backStack[i] != Route.Homepage) backStack.removeAt(i)
                }
            }
        } catch (_: HttpRequestException) {
            showSnackBar("Please connect to the Internet!")
        } catch (_: NullPointerException) {
            showSnackBar("Error fetching user data. Please try again later.")
        }
    }

    //SuccessPage stuff
    var successTitle by remember { mutableStateOf("") }
    var successImage by remember {mutableStateOf<@Composable () -> Unit>({})}
    var successContent by remember {mutableStateOf<@Composable () -> Unit>({})}
    var timerEnded by remember {mutableStateOf(false)}

    var showGallery by remember { mutableStateOf(false) }
    // Please do switch over to the non-deprecated version in the future
    // val galleryPicker = rememberImagePickerKMP()
    if (showGallery) {
        GalleryPickerLauncher( // This is what's deprecated
            onPhotosSelected = { photos ->
                showGallery = false
                val selectedImage = photos[0]

                coroutineScope.launch {
                    try {
                        val currentUser = user ?: return@launch

                        val imageBytes = selectedImage.loadBytes()
                        val fileType = selectedImage.fileName?.substringAfterLast(".") ?: "png"

                        val fileName = currentUser.id
                        val bucket = supabase.storage.from("avatars")

                        bucket.upload(fileName, imageBytes) {
                            upsert = true
                            contentType = ContentType.parse("image/$fileType")
                        }

                        val publicUrl = bucket.publicUrl(fileName)
                        val cacheBustedUrl = "$publicUrl?t=${Clock.System.now().toEpochMilliseconds()}"
                        // This combined with the ImageBuilder stuff in AsyncImage should make sure
                        // the image is always up to date.

                        supabase.from("profiles").update(
                            {
                                set("avatar_url", cacheBustedUrl)
                            }
                        ) {
                            filter { eq("id", currentUser.id) }
                        }

                        showSnackBar("Profile photo updated! Refresh to view.")
                        viewModel.awardAchievement(5)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        showSnackBar("Failed to upload image. Please check your connection.")
                    }
                }
            },
            onError = { error ->
                showGallery = false
                error.message?.let { showSnackBar(it) }
            },
            onDismiss = { showGallery = false },
            enableCrop = true,
            mimeTypes = listOf(MimeType.IMAGE_ALL),
            mimeTypeMismatchMessage = "Only allows images"
        )
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when(key){
                // Same here!
                Route.SplashScreen -> {
                    NavEntry(key){
                        SplashScreen(
                            modifier = Modifier.fillMaxSize(),
                            onEnd = {
                                coroutineScope.launch {
                                    try {
                                        val currentSession = supabase.auth.currentSessionOrNull()
                                        val nextRoute = if (timerEnded) {
                                            Route.SuccessPage
                                        } else if (currentSession != null || appPrefs.signedIn.first()){
                                            Route.Homepage
                                        } else if (appPrefs.firstTimeUser.first()) {
                                            Route.SignUpPage
                                        } else {
                                            Route.SignInPage
                                        }
                                        backStack.add(nextRoute)
                                    } catch (e: HttpRequestException) {
                                        e.printStackTrace()
                                        if (appPrefs.signedIn.first()) backStack.add(Route.Homepage)
                                        else backStack.add(Route.SignInPage)
                                    } finally {
                                        backStack.remove(key)
                                    }
                                }
                            },
                            length = splashLength
                        )
                    }
                }
                Route.ThemeTest -> {
                    NavEntry(key) {
                        ThemeTest(
                            onReturn = {
                                backStack.remove(key)
                            },
                            onSignOut = { onSignOut(key) }
                        )
                    }
                }
                Route.SignUpPage -> {
                    NavEntry(key) {
                        SignUpPage(
                            onSignInClicked = { emailOrUsername, password ->
                                backStack.remove(key)
                                backStack.add(Route.SignInPage)
                                if (isEmailValid(emailOrUsername)) sharedEmail = emailOrUsername
                                else sharedUsername = emailOrUsername
                                sharedPassword = password
                            },
                            onSignIn = { onSignIn(it, key) },
                            fromEmail = sharedEmail,
                            fromUsername = sharedUsername,
                            fromPassword = sharedPassword
                        )
                    }
                }
                Route.SignInPage -> {
                    NavEntry(key) {
                        SignInPage(
                            onSignUpClicked = { email, password ->
                                backStack.remove(key)
                                backStack.add(Route.SignUpPage)
                                sharedEmail = email
                                sharedPassword = password
                            },
                            onSignIn = { onSignIn(it, key) },
                            fromEmail = sharedEmail,
                            fromPassword = sharedPassword
                        )
                    }
                }
                Route.Homepage -> {
                    NavEntry(key){
                        Homepage(
                            onSignOut = { onSignOut(key) },
                            viewModel = viewModel,
                            showSnackBar = {msg -> showSnackBar(msg)},
                            onAddGroup = {
                                backStack.add(Route.AddGroupPage)
                            },
                            onGroupClicked = { group ->
                                viewModel.setGroupInFocus(group)
                                backStack.add(Route.GroupDetailsPage)
                            },
                            onAssignmentClicked = { assignment ->
                                viewModel.setAssignmentInFocus(assignment)
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                backStack.add(Route.AddAssignmentPage)
                            },
                            onTimerStart = { assignment ->
                                startTimer(assignment)
                            },
                            onSavedTimerStart = {
                                startTimer(it)
                            },
                            onViewPhoto = {
                                backStack.add(Route.ImageViewPage)
                            },
                            onEditPhoto = {
                                showGallery = true
                            }
                        )
                    }
                }
                Route.AddGroupPage -> {
                    NavEntry(key) {
                        AddGroupPage(
                            user = user ?: error("User is null"),
                            onJoin = { group ->
                                successTitle = "Group joined/created successfully!"
                                successImage = {
                                    Image(
                                        painter = painterResource(Res.drawable.success),
                                        contentDescription = null,
                                    )
                                }
                                successContent = {
                                    GroupDetailsPage(
                                        group,
                                        modifier = Modifier,
                                        showActions = false
                                    )
                                }
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                                coroutineScope.launch {
                                    if (group.owner == user?.id) viewModel.awardAchievement(2)
                                    else viewModel.awardAchievement(3)
                                }
                            },
                            showSnackBar = {showSnackBar(it)}
                        )
                    }
                }
                Route.GroupDetailsPage -> {
                    NavEntry(key) {
                        GroupDetailsPage(
                            group = groupInFocus ?: error("Group is null"),
                            user = user,
                            onAssignmentClicked = { assignment ->
                                viewModel.setAssignmentInFocus(assignment)
                                //Clean up and remove previous assignment details
                                backStack.remove(Route.AssignmentDetailsPage)
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                backStack.add(Route.AddAssignmentPage)
                            },
                            onEdit = {
                                backStack.add(Route.EditGroupPage)
                            },
                            onFinish = {
                                backStack.remove(key)
                                refresh()
                                showSnackBar(it)
                            }
                        )
                    }
                }
                Route.EditGroupPage -> {
                    NavEntry(key){
                        EditGroupPage(
                            group = groupInFocus ?: error("Group is null"),
                            onSave = { newGroup ->
                                showSnackBar("Group updated successfully!")
                                refresh()
                                viewModel.setGroupInFocus(newGroup)
                                backStack.remove(key)
                            },
                        )
                    }
                }
                Route.AssignmentDetailsPage -> {
                    NavEntry(key) {
                        AssignmentDetailsPage(
                            assignment = assignmentInFocus ?: error("Assignment is null"),
                            user = user,
                            onGroupClicked = { group ->
                                viewModel.setGroupInFocus(group)
                                //Clean up and remove previous group details
                                backStack.remove(Route.GroupDetailsPage)
                                backStack.add(Route.GroupDetailsPage)
                            },
                            onDo = { assignment ->
                                viewModel.setAssignmentInFocus(assignment)
                                backStack.add(Route.TimerDetailsPage)
                            },
                            onEdit = {
                                backStack.add(Route.EditAssignmentPage)
                            },
                            onDelete = {
                                showSnackBar("Assignment deleted!")
                                refresh()
                                backStack.remove(key)
                            }
                        )
                    }
                }
                Route.AddAssignmentPage -> {
                    NavEntry(key) {
                        AddAssignmentPage(
                            startingGroup = groupInFocus,
                            user = user ?: error("User is null"),
                            showSnackBar = {showSnackBar(it)},
                            onAdd = {
                                successTitle = "Assignment created!"
                                successImage = {
                                    Image(
                                        painter = painterResource(Res.drawable.success),
                                        contentDescription = null,
                                    )
                                }
                                successContent = {
                                    AssignmentDetailsPage(
                                        it,
                                        modifier = Modifier,
                                        showActions = false
                                    )
                                }
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                                refresh()
                            }
                        )
                    }
                }
                Route.EditAssignmentPage -> {
                    NavEntry(key){
                        EditAssignmentPage(
                            assignment = assignmentInFocus ?: error("Assignment is null"),
                            onSave = {
                                showSnackBar("Assignment updated successfully!")
                                backStack.remove(key)
                                backStack.remove(Route.AssignmentDetailsPage)
                                refresh()
                            }
                        )
                    }
                }
                Route.TimerPage -> {
                    NavEntry(key){
                        Timer(
                            userAssignment = userAssignmentInFocus ?: error("UserAssignment is null"),
                            onFinish = { userAssignment ->
                                backStack.remove(key)
                                userAssignment?.let {
                                    val studsToAdd = it.iterations * it.period
                                    splashLength = SplashLength.LONG
                                    timerEnded = true
                                    successTitle = if (it.completed) "Yay, you did it!" else "Saved for later!"
                                    successImage = {
                                        Image(
                                            painter = painterResource(
                                                if (it.completed) Res.drawable.congrats
                                                else Res.drawable.success
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(15.dp)
                                                .aspectRatio(1f)
                                        )
                                    }
                                    successContent = {
                                        Text(
                                            text = if (it.completed) "+$studsToAdd Studs"
                                            else "You can access this session again from the homepage!",
                                            style = MaterialTheme.typography.headlineSmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    backStack.remove(key)
                                    backStack.add(Route.SplashScreen)
                                    backStack.remove(Route.Homepage)
                                    if (it.completed) try { viewModel.endTimer(it, studsToAdd) }
                                    catch(e: Exception) { showSnackBar(e.message?: "An error occurred.") }
                                }
                            }
                        )
                    }
                }
                Route.TimerDetailsPage -> {
                    NavEntry(key) {
                        Column{
                            Spacer(Modifier.height(20.dp))
                            TimerDetails(
                                assignments = emptyList(),
                                startingAssignment = assignmentInFocus,
                                onStart = {
                                    startTimer(it)
                                },
                                onStartSaved = {
                                    startTimer(it)
                                },
                                selectable = false
                            )
                        }
                    }
                }
                Route.SuccessPage -> {
                    NavEntry(key) {
                        SuccessPage(
                            title = successTitle,
                            image = successImage,
                            content = successContent,
                            onReturn = {
                                timerEnded = false
                                backStack.add(Route.Homepage)
                                backStack.remove(key)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(15.dp)
                                .padding(bottom = 20.dp)
                        )
                    }
                }
                Route.Leaderboard -> {
                    NavEntry(key){
                        Leaderboard(
                            user = user ?: error("User is null")
                        )
                    }
                }
                Route.SettingsPage -> {
                    NavEntry(key){
                        SettingsPage(
                            appPrefs = appPrefs,
                            viewModel = settingsViewModel,
                        )
                    }
                }
                Route.ImageViewPage -> {
                    NavEntry(key){
                        ImageView(
                            user = user ?: error("User is null"),
                            onReturn = {
                                backStack.remove(key)
                            },
                            onEdit = {
                                showGallery = true
                            }
                        )
                    }
                }
                Route.AchievementsPage -> {
                    NavEntry(key){
                        AchievementsPage(
                            allAchievements = viewModel.allAchievements.value,
                            userAchievements = user?.achievements ?: error("User is null")
                        )
                    }
                }
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}