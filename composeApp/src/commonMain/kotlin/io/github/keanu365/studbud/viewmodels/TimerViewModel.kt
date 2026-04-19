package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    val userAssignment: UserAssignment
) : AlertViewModel() {
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow(TimerState.CONFIRMING)
    val timerState = _timerState.asStateFlow()

    private val _timerMins = MutableStateFlow(userAssignment.period)
    val timerMins = _timerMins.asStateFlow()

    private val _timerSecs = MutableStateFlow(0)
    val timerSecs = _timerSecs.asStateFlow()

    fun startTimer(){
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _timerSecs.emit(0)
            _timerState.emit(TimerState.STARTING)
            delay(2000)
            for (i in 3 downTo 1){
                _timerSecs.emit(i)
                delay(1000)
            }
            runTimerLoop(userAssignment.period)
        }
    }

    fun resumeTimer(mins: Int, secs: Int = 0){
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            runTimerLoop(mins, secs)
        }
    }

    private suspend fun runTimerLoop(initialMins: Int, initialSecs: Int = 0){
        _timerMins.emit(initialMins)
        _timerSecs.emit(initialSecs)
        _timerState.emit(TimerState.RUNNING)
        while (_timerMins.value >= 0 && _timerState.value == TimerState.RUNNING) {
            delay(1000)
            // Check state again after delay in case it was paused during the wait
            if (_timerState.value != TimerState.RUNNING) break

            if (_timerSecs.value > 0) {
                _timerSecs.emit(_timerSecs.value - 1)
            } else if (_timerMins.value > 0) {
                _timerSecs.emit(59)
                _timerMins.emit(_timerMins.value - 1)
            } else {
                break // Timer finished
            }
        }

        if (_timerState.value == TimerState.RUNNING) {
            _timerState.emit(TimerState.INTERMISSION)
        }
    }

    fun setTimerState(state: TimerState) {
        _timerState.value = state
        if (state == TimerState.PAUSED || state == TimerState.FINISHED) {
            timerJob?.cancel() // Stop the loop immediately on pause
        }
    }

    suspend fun getAssignment(): Assignment? {
        return try{
            supabase.from("assignments")
                .select {
                    filter {
                        eq("id", userAssignment.assignment_id)
                    }
                }
                .decodeSingleOrNull<Assignment>()
        } catch (_: Exception){
            null
        }
    }

    suspend fun deleteUserAssignment() {
        if (userAssignment.user_id.isNotBlank()) try {
            supabase.from("user_assignments").delete {
                filter {
                    eq("uuid", userAssignment.uuid)
                }
            }
        } catch (e: Exception) {e.printStackTrace()}
    }

    fun showSaveAlert(){
        _alert.value = {
            Alert(
                title = "Save Assignment",
                text = "Save this assignment for later? All progress will be lost.",
            ){
                setTimerState(TimerState.SAVED)
            }
        }
    }
}

enum class TimerState{
    CONFIRMING, STARTING, RUNNING, PAUSED, INTERMISSION, FINISHED, SAVED
}