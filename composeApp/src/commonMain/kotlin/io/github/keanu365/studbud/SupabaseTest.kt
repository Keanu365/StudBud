package io.github.keanu365.studbud

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.viewmodels.SupabaseTestViewModel

@Composable
fun SampleData(
    viewModel: SupabaseTestViewModel = viewModel { SupabaseTestViewModel() }
){
    val sampleData by viewModel.sampleData.collectAsStateWithLifecycle()
    LaunchedEffect(Unit){
        viewModel.fetchSampleData()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ){
        sampleData.forEach { test ->
            Card(
                modifier = Modifier.padding(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ){
                Column {
                    Text(test.name, fontWeight = FontWeight.Bold)
                    Text(test.text)
                }
            }
        }
    }
}