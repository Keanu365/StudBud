package io.github.keanu365.studbud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.theme.BgBlack
import io.github.keanu365.studbud.theme.buttonColors
import io.github.keanu365.studbud.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_down
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Preview
@Composable
fun Onboarding(
    viewModel: OnboardingViewModel = viewModel { OnboardingViewModel() },
    onFinish: () -> Unit = {},
){
    val onboardingScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { viewModel.pages.size })
    val isLastPage = pagerState.currentPage == viewModel.pages.size - 1

    val bgColorAnimation = animateColorAsState(
        targetValue = viewModel.pages[pagerState.currentPage].bgColor,
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseOutSine
        )
    )
    Box(modifier = Modifier.fillMaxSize()){
        StarBackground(
            bgColor = bgColorAnimation.value,
            modifier = Modifier.fillMaxSize()
        )
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ){ page ->
            Box(Modifier.fillMaxSize()){
                Image(
                    painter = painterResource(viewModel.pages[page].resource),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(25.dp)
                        .aspectRatio(1f)
                )
                Text(
                    text = viewModel.pages[page].title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 10.dp)
                        .padding(top = 20.dp)
                )
                Text(
                    text = viewModel.pages[page].desc,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxHeight(0.35f)
                        .padding(horizontal = 25.dp)
                )
            }
        }
        //Actions
        Button(
            onClick = onFinish,
            colors = buttonColors().copy(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(15.dp)
        ){
            Text("Skip")
        }
        TertiaryButton(
            onClick = {
                onboardingScope.launch {
                    if (!isLastPage) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    else onFinish()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(15.dp)
        ){
            Text(if (isLastPage) "Get Started" else "Next")
        }
        //Counter and other things
        Text(
            text = "${pagerState.currentPage + 1}/${viewModel.pages.size}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
        val offset = rememberInfiniteTransition().animateFloat(
            initialValue = -15f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            )
        )
        Icon(
            painter = painterResource(Res.drawable.icon_down),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 65.dp)
                .size(48.dp)
                .offset(y = offset.value.dp)
                .alpha(if (isLastPage) 0f else 1f)
        )
    }
}

@Composable
private fun StarBackground(
    modifier: Modifier = Modifier,
    bgColor: Color = BgBlack
) {
    data class Star(
        val x: Float,
        val y: Float,
        val phaseOffset: Float
    )

    val infiniteTransition = rememberInfiniteTransition()
        val time = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val stars = remember(widthPx, heightPx) {
            buildList {
                repeat(1000) {
                    add(
                        Star(
                            x = Random.nextFloat() * widthPx,
                            y = Random.nextFloat() * heightPx,
                            phaseOffset = Random.nextFloat() * (2 * PI).toFloat()
                        )
                    )
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(colors = listOf(bgColor, BgBlack))
            )

            for (star in stars) {
                val alpha = (sin(time.value + star.phaseOffset) + 1f) / 2f

                drawCircle(
                    color = Color.White,
                    center = Offset(star.x, star.y),
                    radius = 2.5f,
                    alpha = alpha
                )
            }
        }
    }
}