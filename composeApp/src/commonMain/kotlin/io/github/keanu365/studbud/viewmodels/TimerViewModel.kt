package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.UserAssignment
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel(
    val userAssignment: UserAssignment
) : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState.CONFIRMING)
    val timerState = _timerState.asStateFlow()

    private val _timerMins = MutableStateFlow(userAssignment.period)
    val timerMins = _timerMins.asStateFlow()

    private val _timerSecs = MutableStateFlow(0)
    val timerSecs = _timerSecs.asStateFlow()

    suspend fun startTimer(){
        _timerSecs.emit(0)
        _timerState.emit(TimerState.STARTING)
        delay(2000)
        for (i in 3 downTo 1){
            _timerSecs.emit(i)
            delay(1000)
        }
        restartTimer(userAssignment.period)
    }

    suspend fun restartTimer(initialMins: Int, initialSecs: Int = 0){
        _timerMins.emit(initialMins)
        _timerSecs.emit(initialSecs)
        _timerState.emit(TimerState.RUNNING)
        delay(1000)
        while (_timerMins.value >= 0 && _timerState.value == TimerState.RUNNING){
            _timerSecs.emit(_timerSecs.value - 1)
            if (_timerSecs.value < 0){
                _timerSecs.emit(59)
                _timerMins.emit(_timerMins.value - 1)
            }
            delay(1000)
        }
        _timerState.emit(TimerState.PAUSED)
    }

    suspend fun setTimerState(state: TimerState) = _timerState.emit(state)

    suspend fun getAssignment(): Assignment? {
        return try{
            supabase.from("assignments")
                .select {
                    filter {
                        eq("id", userAssignment.id)
                    }
                }
                .decodeSingleOrNull<Assignment>()
        } catch (_: Exception){
            null
        }
    }
}

enum class TimerState{
    CONFIRMING, STARTING, RUNNING, PAUSED, FINISHED
}