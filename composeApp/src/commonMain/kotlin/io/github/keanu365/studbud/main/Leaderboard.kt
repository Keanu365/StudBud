package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.DataView
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.viewmodels.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Leaderboard(
    user: User,
    viewModel: LeaderboardViewModel = viewModel { LeaderboardViewModel(user) }
){
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    var isGroupsExpanded by remember { mutableStateOf(false) }
    val groupMembers by viewModel.groupMembers.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(Modifier.height(25.dp))
        TitleText("Leaderboard")
        ExposedDropdownMenuBox(
            expanded = isGroupsExpanded,
            onExpandedChange = { isGroupsExpanded = !isGroupsExpanded },
        ) {
            InfoField(
                value = selectedGroup.name,
                onValueChange = {},
                readOnly = true,
                isError = false,
                labelText = "Group",
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupsExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isGroupsExpanded,
                onDismissRequest = { isGroupsExpanded = false },
                modifier = Modifier
                    .heightIn(max = 300.dp)
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = {
                            viewModel.setGroup(group)
                            isGroupsExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        DataView(
            dataList = groupMembers,
            firstLabel = "Name",
            secondLabel = "All-time Studs",
            isLeaderboard = true,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
    }
}