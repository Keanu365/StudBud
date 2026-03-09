package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Test(
    val name: String,
    val text: String
)

class SupabaseTestViewModel : ViewModel() {
    private val _sampleData = MutableStateFlow<List<Test>>(emptyList())
    val sampleData = _sampleData.asStateFlow()

    fun fetchSampleData() {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    supabase.from("sample_data")
                        .select().decodeList<Test>()
                }
                _sampleData.value = data
            } catch (e: Exception) {
                // Log the error to see if it's a network/RLS issue
                println("Supabase Error: ${e.message}")
            }
        }
    }
}