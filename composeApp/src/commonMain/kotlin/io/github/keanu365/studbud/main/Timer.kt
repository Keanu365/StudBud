package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.ErrorButton
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.UserAssignment
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

    LaunchedEffect(Unit){
        assignment = viewModel.getAssignment()
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
        TimerState.FINISHED -> {
            //TODO Code once timer is finished
            onFinish(userAssignment)
            TitleText("Finished")
        }
        else -> {
            //Placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Column {
                    Text(
                        text = "${if (mins < 10) "0$mins" else mins} : ${if (secs < 10) "0$secs" else secs}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TertiaryButton(
                        onClick = {
                            timerScope.launch {
                                if (state == TimerState.RUNNING) viewModel.setTimerState(TimerState.PAUSED)
                                else viewModel.restartTimer(mins, secs)
                            }
                        }
                    ){
                        Text("Pause")
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
}

@Composable
private fun ConfirmationPage(
    userAssignment: UserAssignment,
    assignmentName: String = "None",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
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