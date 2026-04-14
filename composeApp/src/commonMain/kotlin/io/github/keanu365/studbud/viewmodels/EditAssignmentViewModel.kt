package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class EditAssignmentViewModel(
    assignment: Assignment
): ViewModel() {
    private val _newAssignment = MutableStateFlow(assignment)
    val newAssignment = _newAssignment.asStateFlow()
    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker = _showDatePicker.asStateFlow()

    fun setName(name: String){
        _newAssignment.value = _newAssignment.value.copy(name = name)
    }
    fun setDescription(desc: String){
        _newAssignment.value = _newAssignment.value.copy(description = desc)
    }
    fun setDueDate(epochMillis: Long?){
        epochMillis?.let{
            val dueDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(UTC).date
            _newAssignment.value = _newAssignment.value.copy(due_date = dueDate)
        }
    }
    fun setShowDatePicker(show: Boolean){
        _showDatePicker.value = show
    }
    
    fun saveAssignment(){
        viewModelScope.launch {
            try {
                supabase.from("assignments").update({
                    set("name", _newAssignment.value.name)
                    set("description", _newAssignment.value.description)
                    set("due_date", _newAssignment.value.due_date)
                }) {
                    filter { eq("id", _newAssignment.value.id) }
                }
                supabase.from("assignments").upsert(_newAssignment.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}