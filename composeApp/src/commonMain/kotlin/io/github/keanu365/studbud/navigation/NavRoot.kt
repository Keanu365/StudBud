package io.github.keanu365.studbud.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.AutoUserAssignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.SplashLength
import io.github.keanu365.studbud.SplashScreen
import io.github.keanu365.studbud.SuccessPage
import io.github.keanu365.studbud.ThemeTest
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.account.SignInPage
import io.github.keanu365.studbud.account.SignUpPage
import io.github.keanu365.studbud.account.isEmailValid
import io.github.keanu365.studbud.main.AddAssignmentPage
import io.github.keanu365.studbud.main.AddGroupPage
import io.github.keanu365.studbud.main.AssignmentDetailsPage
import io.github.keanu365.studbud.main.GroupDetailsPage
import io.github.keanu365.studbud.main.Homepage
import io.github.keanu365.studbud.main.ImageView
import io.github.keanu365.studbud.main.SettingsPage
import io.github.keanu365.studbud.main.Timer
import io.github.keanu365.studbud.main.TimerDetails
import io.github.keanu365.studbud.supabase
import io.github.keanu365.studbud.viewmodels.SettingsViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.congrats
import studbud.composeapp.generated.resources.success

@Composable
fun NavRoot(
    appPrefs: AppPreferences,
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(appPrefs = appPrefs) },
    showSnackBar: (String) -> Unit,
    changeTopBar: (Boolean, Boolean, Boolean) -> Unit = { _, _, _ -> } //showBar, showBack, showActions){}
){
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var groupInFocus by remember { mutableStateOf<Group?>(null) }
    var assignmentInFocus by remember { mutableStateOf<Assignment?>(null) }
    var userAssignmentInFocus by remember { mutableStateOf<UserAssignment?>(null) }

    var sharedEmail by remember {mutableStateOf("")}
    var sharedUsername by remember {mutableStateOf("")}
    var sharedPassword by remember {mutableStateOf("")}

    var splashLength by remember {mutableStateOf(SplashLength.MEDIUM)}
    fun onSignIn(user: User, key: NavKey) = run {
        coroutineScope.launch {
            appPrefs.saveSignIn(user.id)
            appPrefs.setFirstTimeUser(false)
            //TODO change this implementation when you do onboarding
        }
        sharedEmail = ""
        sharedUsername = ""
        sharedPassword = ""
        showSnackBar("Signed in successfully!")
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }
    fun onSignOut(key: NavKey) = run {
        coroutineScope.launch {
            supabase.auth.signOut()
            appPrefs.signOut()
        }
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }
    fun startTimer(assignment: AutoUserAssignment){
        coroutineScope.launch {
            try {
                user?.let{user ->
                    userAssignmentInFocus = if (assignment.assignment_id.isBlank())
                        UserAssignment(
                            user_id = user.id,
                            period = assignment.period,
                            breaktime = assignment.breaktime,
                            iterations = assignment.iterations
                        )
                    else supabase.from("user_assignments")
                        .insert(assignment){select()}
                        .decodeSingle<UserAssignment>()
                } ?: showSnackBar("Timer cannot be started as user is null.")
                backStack.add(Route.TimerPage)
                for (i in backStack.size-2 downTo 0){
                    if (backStack[i] != Route.Homepage) backStack.removeAt(i)
                }
            } catch (_: HttpRequestException) {
                showSnackBar("Please connect to the Internet!")
            } catch (_: NullPointerException) {
                showSnackBar("Error fetching user data. Please try again later.")
            }
        }
    }

    //SuccessPage stuff
    var successTitle by remember { mutableStateOf("") }
    var successImage by remember {mutableStateOf<@Composable () -> Unit>({})}
    var successContent by remember {mutableStateOf<@Composable () -> Unit>({})}
    var timerEnded by remember {mutableStateOf(false)}

    //Indicate here if you want back arrow / actions / top bar
    val showBackKeys = listOf(
        Route.AddGroupPage,
        Route.GroupDetailsPage,
        Route.AssignmentDetailsPage,
        Route.AddAssignmentPage,
        Route.TimerDetailsPage,
        Route.SettingsPage
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
    LaunchedEffect(backStack.last()){
        changeTopBar(
            !hideTopBar.contains(backStack.last()),
            showBackKeys.contains(backStack.last()),
            showActionsKeys.contains(backStack.last())
        )
    }

    var showGallery by remember { mutableStateOf(false) }
    if (showGallery) {
        GalleryPickerLauncher(
            onPhotosSelected = { photos ->
                showGallery = false
                val selectedImage = photos[0]

                coroutineScope.launch {
                    try {
                        val currentUser = user ?: return@launch

                        // 1. Convert the Picker Result to ByteArray (KMP friendly)
                        val imageBytes = selectedImage.loadBytes()
                        val fileType = selectedImage.fileName?.substringAfterLast(".") ?: "png"

                        // 2. Define a unique path
                        // Using user.id + timestamp ensures unique filenames and prevents stale caches
                        val fileName = currentUser.id
                        val bucket = supabase.storage.from("avatars")

                        // 3. Upload the ByteArray to Supabase Storage
                        bucket.upload(fileName, imageBytes) {
                            upsert = true
                            contentType = ContentType.parse("image/$fileType")
                        }

                        // 4. Get the Public URL of the new photo
                        val publicUrl = bucket.publicUrl(fileName)

                        // 5. Update the 'profiles' table with the new URL
                        supabase.from("profiles").update(
                            {
                                set("avatar_url", publicUrl)
                            }
                        ) {
                            filter { eq("id", currentUser.id) }
                        }

                        // 6. Update local state so the UI reflects the change immediately
                        user = currentUser.copy(avatar_url = publicUrl)
                        showSnackBar("Profile photo updated!")

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
                            appPrefs = appPrefs,
                            showSnackBar = {showSnackBar(it)},
                            onUserLoaded = {user = it},
                            onAddGroup = {
                                user = it
                                backStack.add(Route.AddGroupPage)
                            },
                            onGroupClicked = {
                                groupInFocus = it
                                backStack.add(Route.GroupDetailsPage)
                            },
                            onAssignmentClicked = {
                                assignmentInFocus = it
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                user = it
                                backStack.add(Route.AddAssignmentPage)
                            },
                            onTimerStart = { assignment ->
                                startTimer(assignment)
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
                            onJoin = {
                                successTitle = "Group joined/created successfully!"
                                successImage = {
                                    Image(
                                        painter = painterResource(Res.drawable.success),
                                        contentDescription = null,
                                    )
                                }
                                successContent = {GroupDetailsPage(it, modifier = Modifier)}
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                            },
                            showSnackBar = {showSnackBar(it)}
                        )
                    }
                }
                Route.GroupDetailsPage -> {
                    NavEntry(key) {
                        GroupDetailsPage(
                            group = groupInFocus ?: error("Group is null"),
                            onAssignmentClicked = {
                                assignmentInFocus = it
                                //Clean up and remove previous assignment details
                                backStack.remove(Route.AssignmentDetailsPage)
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                backStack.add(Route.AddAssignmentPage)
                            }
                        )
                    }
                }
                Route.AssignmentDetailsPage -> {
                    NavEntry(key) {
                        AssignmentDetailsPage(
                            assignment = assignmentInFocus ?: error("Assignment is null"),
                            onGroupClicked = {
                                groupInFocus = it
                                //Clean up and remove previous group details
                                backStack.remove(Route.GroupDetailsPage)
                                backStack.add(Route.GroupDetailsPage)
                            },
                            onDo = { assignment ->
                                assignmentInFocus = assignment
                                backStack.add(Route.TimerDetailsPage)
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
                                    AssignmentDetailsPage(it, modifier = Modifier)
                                }
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                            }
                        )
                    }
                }
                Route.TimerPage -> {
                    NavEntry(key){
                        Timer(
                            userAssignment = userAssignmentInFocus ?: error("UserAssignment is null"),
                            onFinish = { userAssignment ->
                                if (userAssignment == null) {
                                    backStack.remove(key)
                                }
                                else {
                                    val studsToAdd = userAssignment.iterations * userAssignment.period
                                    splashLength = SplashLength.LONG
                                    timerEnded = true
                                    successTitle = "Yay, you did it!"
                                    successImage = {
                                        Image(
                                            painter = painterResource(Res.drawable.congrats),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(15.dp)
                                                .aspectRatio(1f)
                                        )
                                    }
                                    successContent = {
                                        Text(
                                            text = "+$studsToAdd Studs",
                                            fontSize = 36.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    backStack.remove(key)
                                    backStack.add(Route.SplashScreen)
                                    backStack.remove(Route.Homepage)
                                    coroutineScope.launch {
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
                                            user = supabase.from("profiles")
                                                .update(
                                                    {
                                                        set("studs", user!!.studs + studsToAdd)
                                                        set("all_time_studs", user!!.all_time_studs + studsToAdd)
                                                    }
                                                ){
                                                    filter {
                                                        eq("id", user!!.id)
                                                    }
                                                    select()
                                                }
                                                .decodeSingle<User>()
                                        } catch (_: HttpRequestException) {
                                            showSnackBar("Failed to save studs to database. Studs will be added the next time you're connected to the internet.")
                                        } catch (_: NullPointerException) {
                                            showSnackBar("User could not be found. Studs will be added the next time you're connected to the internet.")
                                        }
                                    }
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
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}