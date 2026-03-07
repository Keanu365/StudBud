package io.github.keanu365.studbud

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.viewmodels.SplashScreenViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.studbud
import kotlin.random.Random

@Composable
fun SplashScreen(
    viewModel: SplashScreenViewModel = viewModel { SplashScreenViewModel() },
    onEnd: () -> Unit
){
    val screenShowTime = remember { Random.nextInt(2000, 8000) }
    val tips by viewModel.tips.collectAsStateWithLifecycle()
    var animationChoice by remember { mutableStateOf(LogoAnimation.random()) }
    var tip by remember { mutableStateOf(tips[Random.nextInt(tips.size)]) }

    LaunchedEffect(Unit){
        delay(screenShowTime.toLong())
        onEnd()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable{
                    animationChoice = LogoAnimation.random()
                    tip = tips[Random.nextInt(tips.size)]
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            AnimatedLogo(
                animationChoice = animationChoice,
                modifier = Modifier.padding(bottom = 100.dp)
            )
            Text(
                text = tip,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

enum class LogoAnimation {
    SIZE, ROTATE, ALPHA, TRANSLATE;

    companion object {
        fun random() = entries[Random.nextInt(entries.size)]
    }
}

@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    animationChoice: LogoAnimation,
){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.height(500.dp).fillMaxWidth()
    ){
        Image(
            painter = painterResource(Res.drawable.studbud),
            contentDescription = "StudBud Logo",
            modifier = animateLogo(animationChoice)
                .then(modifier)
                .padding(20.dp)
        )
    }
}

@Composable
private fun animateLogo(
    animation: LogoAnimation
): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val sizeAnimation = if (animation != LogoAnimation.SIZE) null else infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotateAnimation = if (animation != LogoAnimation.ROTATE) null else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutBack),
            repeatMode = RepeatMode.Restart
        )
    )
    val alphaAnimation = if (animation != LogoAnimation.ALPHA) null else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )
    val translateAnimation = if (animation != LogoAnimation.TRANSLATE) null else infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )
    return Modifier
        .fillMaxSize(sizeAnimation?.value ?: 1f)
        .rotate(rotateAnimation?.value ?: 0f)
        .alpha(alphaAnimation?.value ?: 1f)
        .offset(y = translateAnimation?.value?.dp ?: 0.dp)
}
