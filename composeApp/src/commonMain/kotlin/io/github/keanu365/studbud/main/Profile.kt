package io.github.keanu365.studbud.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.keanu365.studbud.ErrorButton
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.getDeviceType
import io.github.keanu365.studbud.theme.errorButtonColors
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.default
import studbud.composeapp.generated.resources.icon_error
import studbud.composeapp.generated.resources.icon_replace_image
import studbud.composeapp.generated.resources.icon_visible

@Composable
fun Profile(
    onSignOut: () -> Unit,
    user: User? = null,
    onViewPhoto: () -> Unit = {},
    onEditPhoto: () -> Unit = {},
){
    val density = LocalDensity.current
    var tapOffset by remember {mutableStateOf(Offset.Zero)}
    var showSignOutAlert by remember {mutableStateOf(false)}
    var showDropdownMenu by remember {mutableStateOf(false)}
    if (showSignOutAlert){
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(50.dp)
                )
            },
            title = {
                Text("Sign Out")
            },
            text = {
                Text("Are you sure you want to sign out?")
            },
            onDismissRequest = {showSignOutAlert = false},
            confirmButton = {
                Button(
                    onClick = {
                        onSignOut()
                        showSignOutAlert = false
                    },
                    colors = errorButtonColors()
                ){Text("Yes")}
            },
            dismissButton = {
                Button(
                    onClick = {showSignOutAlert = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ){Text("No")}
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ){
            Box{
                user?.let{ user ->
                    AsyncImage(
                        model = if (getDeviceType() != "Desktop") user.avatar_url else ImageRequest.Builder(LocalPlatformContext.current)
                            .data(user.avatar_url)
                            .memoryCachePolicy(CachePolicy.DISABLED) // Forces it to ignore RAM cache
                            .diskCachePolicy(CachePolicy.DISABLED)   // Forces it to ignore Disk cache
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(bottom = 30.dp)
                            .height(250.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    tapOffset = it
                                    showDropdownMenu = true
                                }
                            }
                    )
                } ?: Image(
                    painter = painterResource(Res.drawable.default),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .height(250.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                tapOffset = it
                                showDropdownMenu = true
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { tapOffset.x.toDp() },
                            y = with(density) { tapOffset.y.toDp() }
                        )
                        .size(0.dp) // Invisible anchor
                ) {
                    PhotoMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        onView = onViewPhoto,
                        onEdit = onEditPhoto
                    )
                }
            }
            Text(
                text = user?.username ?: "Unknown User",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = user?.email ?: "Unknown Email",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Current Stud Balance: ${user?.studs ?: 0}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "All-time Studs: ${user?.all_time_studs ?: 0}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "That's all for now. Stay tuned!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(50.dp))
        }
        ErrorButton(
            onClick = {
                showSignOutAlert = true
            },
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "SIGN OUT",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PhotoMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
){
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ){
        DropdownMenuItem(
            text = {Text("View Photo")},
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_visible),
                    contentDescription = null
                )
            },
            onClick = onView
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = {Text("Change Photo")},
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_replace_image),
                    contentDescription = null
                )
            },
            onClick = onEdit
        )
    }
}