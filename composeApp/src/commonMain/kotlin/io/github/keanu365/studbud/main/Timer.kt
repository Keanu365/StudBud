package io.github.keanu365.studbud.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.ErrorButton
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.theme.buttonColors
import io.github.keanu365.studbud.viewmodels.TimerState
import io.github.keanu365.studbud.viewmodels.TimerViewModel
import kotlinx.coroutines.launch

@Composable
fun Timer(
    userAssignment: UserAssignment,
    viewModel: TimerViewModel = viewModel { TimerViewModel(userAssignment) },
    onFinish: (UserAssignment?) -> Unit //null -> dismissed, else saved/finished
){
    val timerScope = rememberCoroutineScope()
    var assignment by remember { mutableStateOf<Assignment?>(null) }

    val state by viewModel.timerState.collectAsStateWithLifecycle()
    val mins by viewModel.timerMins.collectAsStateWithLifecycle()
    val secs by viewModel.timerSecs.collectAsStateWithLifecycle()
    var iterations by remember { mutableStateOf(1) }
    var nextTimer by remember {mutableStateOf("${userAssignment.breaktime} minute break")}
    val value = (mins*60f + secs) / (userAssignment.period * 60f)

    LaunchedEffect(Unit){
        assignment = viewModel.getAssignment()
    }
    LaunchedEffect(mins){
        if (mins < 0) {
            viewModel.setTimerState(TimerState.INTERMISSION)
        }
    }
    LaunchedEffect(iterations){
        if (iterations > userAssignment.iterations){
            viewModel.setTimerState(TimerState.FINISHED)
        }
    }

    when(state){
        TimerState.CONFIRMING -> ConfirmationPage(
            userAssignment = userAssignment,
            assignmentName = assignment?.name ?: "None",
            onConfirm = {
                timerScope.launch {
                    viewModel.startTimer()
                }
            },
            onDismiss = {onFinish(null)}
        )
        TimerState.STARTING -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = if (secs == 0) "Ready?" else "$secs",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        TimerState.INTERMISSION -> {
            IntermissionPage(
                next = nextTimer,
                iterations = "$iterations/${userAssignment.iterations}",
                onContinue = {
                    var nextMins = 0
                    nextTimer = if (nextTimer.endsWith("break")) "${userAssignment.period} minute study"
                        .also{nextMins = userAssignment.breaktime}
                    else "${userAssignment.breaktime} minute break"
                        .also{nextMins = userAssignment.period; iterations++}
                    viewModel.resumeTimer(nextMins)
                }
            )
        }
        TimerState.FINISHED -> {
            onFinish(userAssignment)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                TitleText("Finished")
            }
        }
        else -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().keepScreenOn()
            ) {
                TitleText(
                    text = assignment?.name ?: "Study Session"
                )
                Spacer(Modifier.height(15.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    TimerCountdown(
                        value = value,
                        modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f)
                    )
                    Text(
                        text = "${if (mins < 10) "0$mins" else mins} : ${if (secs < 10) "0$secs" else secs}",
                        fontSize = 75.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TimerStatus(
                    next = nextTimer,
                    iterations = "$iterations/${userAssignment.iterations}",
                    modifier = Modifier.fillMaxWidth().padding(15.dp)
                )
                Button(
                    onClick = {
                        if (state == TimerState.RUNNING) {
                            timerScope.launch { viewModel.setTimerState(TimerState.PAUSED) }
                        } else {
                            viewModel.resumeTimer(mins, secs)
                        }
                    },
                    colors = buttonColors().copy(
                        containerColor = if (state == TimerState.PAUSED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                ){
                    Text(
                        text = if (state == TimerState.RUNNING) "PAUSE" else "RESUME",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                ErrorButton(
                    onClick = {
                        timerScope.launch {
                            viewModel.setTimerState(TimerState.FINISHED)
                        }
                    }
                ){
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun ConfirmationPage(
    userAssignment: UserAssignment,
    assignmentName: String = "None",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        TitleText("Confirmation")
        Text(
            text = "Please click 'Confirm' to start the timer.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Assignment Name:\n$assignmentName",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Period: ${userAssignment.period} minutes",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Break Time: ${userAssignment.breaktime} minutes",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Iterations: ${userAssignment.iterations} minutes",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Total Estimated Completion Time:\n${(userAssignment.period + userAssignment.breaktime) * userAssignment.iterations} minutes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ){
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            TertiaryButton(
                onClick = onConfirm
            ){
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimerCountdown(
    inactiveBarColor: Color = MaterialTheme.colorScheme.surface,
    activeBarColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 10.dp,
    value: Float = 0f,
    modifier: Modifier = Modifier,
){
    Canvas(modifier = modifier) {
        drawArc(
            color = inactiveBarColor,
            startAngle = -215f,
            sweepAngle = 250f,
            useCenter = false,
            size = Size(size.width, size.height),
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = activeBarColor,
            startAngle = -215f,
            sweepAngle = 250f * value,
            useCenter = false,
            size = Size(size.width, size.height),
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun IntermissionPage(
    next: String,
    iterations: String,
    onContinue: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = 15.dp)
    ){
        Spacer(Modifier.height(10.dp))
        TitleText("Good Job!")
        TimerStatus(
            next,
            iterations,
            Modifier.fillMaxWidth()
        )
        Text(
            text = "Press 'Continue' once you are ready to continue.",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TertiaryButton(
            onClick = onContinue
        ){
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimerStatus(
    next: String,
    iterations: String,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = "Next:",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = next,
                fontSize = 24.sp
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = "Iterations:",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = iterations,
                fontSize = 24.sp
            )
        }
    }
}