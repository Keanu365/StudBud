package io.github.keanu365.studbud.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.keanu365.studbud.account.User
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_error

@Composable
fun Profile(
    onSignOut: () -> Unit,
    user: User? = null
){
    var showSignOutAlert by remember {mutableStateOf(false)}
    var showPhotoAlert by remember {mutableStateOf(false)}
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
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
    if (showPhotoAlert){
        AlertDialog(
            title = {
                Text(
                    text = "Profile Picture",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    Text(
                        text = "View Photo",
                        fontSize = 18.sp,
                        modifier = Modifier.clickable{
                            //TODO
                        }
                    )
                    HorizontalDivider(
                        thickness = 3.dp,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                    Text(
                        text = "Change Photo",
                        fontSize = 18.sp,
                        modifier = Modifier.clickable{
                            //TODO
                        }
                    )
                }
            },
            onDismissRequest = {showPhotoAlert = false},
            confirmButton = {}, //Not needed for our purposes
            dismissButton = {
                Button(
                    onClick = {showPhotoAlert = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ){Text("Cancel")}
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
            AsyncImage(
                model = user?.avatar_url
                    ?: "https://dyikkrnyteudomofjrdz.supabase.co/storage/v1/object/public/avatars/default.png",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .height(250.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .clickable{showPhotoAlert = true}
            )
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
        }
        Button(
            onClick = {
                showSignOutAlert = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
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