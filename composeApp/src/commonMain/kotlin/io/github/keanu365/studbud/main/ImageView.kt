package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.keanu365.studbud.User
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_arrow_back
import studbud.composeapp.generated.resources.icon_edit

@Composable
fun ImageView(
    user: User,
    modifier: Modifier = Modifier,
    onReturn: () -> Unit,
    onEdit: () -> Unit,
){
    Box(
        modifier = modifier
            .fillMaxSize()
    ){
        val zoomState = rememberZoomState()
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(user.avatar_url)
                .memoryCachePolicy(CachePolicy.DISABLED) // Forces it to ignore RAM cache
                .diskCachePolicy(CachePolicy.DISABLED)   // Forces it to ignore Disk cache
                .build(),
            contentDescription = "Zoomable image",
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
            },
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .zoomable(zoomState),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.TopStart)
        ){
            IconButton(
                onClick = onReturn
            ){
                Icon(
                    painter = painterResource(Res.drawable.icon_arrow_back),
                    contentDescription = "Back Arrow",
                )
            }
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 20.dp),
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ){
                IconButton(
                    onClick = onEdit
                ){
                    Icon(
                        painter = painterResource(Res.drawable.icon_edit),
                        contentDescription = "Edit Photo",
                    )
                }
                // If you have time do Share Image
            }
        }
    }
}