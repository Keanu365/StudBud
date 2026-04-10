package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import io.github.keanu365.studbud.theme.buttonColors
import kotlinx.coroutines.launch
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus

@Composable
fun AddGroupPage(
    user: User,
    onJoin: (Group) -> Unit = {},
    showSnackBar: (String) -> Unit
){
    val networkStatus by rememberNetworkStatus()
    val coroutineScope = rememberCoroutineScope()

    var joinGroupCode by remember { mutableStateOf("") }
    var isJoinGroupCodeError by remember { mutableStateOf(false) }
    var joinErrorMsg by remember { mutableStateOf("Invalid Group Code") }
    fun checkJoin() = run {
        if (networkStatus != NetworkStatus.Available) showSnackBar("You're not connected to a network! Please check your connection and try again.")
        else coroutineScope.launch {
            val group = supabase.from("groups")
                .select {
                    filter {
                        eq("id", joinGroupCode)
                    }
                }
                .decodeSingleOrNull<Group>()
            if (group == null) {
                isJoinGroupCodeError = true
                joinErrorMsg = "Invalid Group Code"
            }
            else if (group.members.contains(user.id)) {
                isJoinGroupCodeError = true
                joinErrorMsg = "You're already a member of this group!"
            } else {
                supabase.from("groups")
                    .update(
                        {
                            set("members", group.members + user.id)
                        }
                    ){
                        filter {
                            eq("id", group.id)
                        }
                    }
                supabase.from("profiles")
                    .update(
                        {
                            set("groups", user.groups?.plus(group.id) ?: listOf(group.id))
                        }
                    ){
                        filter {
                            eq("id", user.id)
                        }
                    }
                onJoin(group)
            }
        }
    }

    var name by remember {mutableStateOf("")}
    var description by remember {mutableStateOf("")} //Will never be error
    //TODO Allow user to add friends as members
    val members = remember { mutableStateListOf(user.id) }
    var nameError by remember {mutableStateOf(false)}
    var nameErrorMsg by remember {mutableStateOf("Please enter a group name!")}
    fun createGroup() = run {
        if (networkStatus != NetworkStatus.Available) showSnackBar("You're not connected to a network! Please check your connection and try again.")
        else coroutineScope.launch {
            if (name.isEmpty()) {
                nameError = true
                nameErrorMsg = "Please enter a group name!"
            } else {
                var groupCode = generateGroupCode()
                while (doesGroupExist(groupCode)) groupCode = generateGroupCode()
                val group = Group(
                    id = groupCode,
                    name = name,
                    description = description,
                    members = members.toList(),
                    owner = user.id
                )
                supabase.from("groups").insert(group){
                    filter {
                        eq("id", user.id)
                    }
                }
                supabase.from("profiles")
                    .update(
                        {
                            set("groups", user.groups?.plus(group.id) ?: listOf(group.id))
                        }
                    ){
                        filter {
                            eq("id", user.id)
                        }
                    }
                onJoin(group)
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(10.dp))
        TitleText("Join A Group")
        InfoField(
            value = joinGroupCode,
            onValueChange = { joinGroupCode = it },
            labelText = "Group Code",
            isError = isJoinGroupCodeError,
            errorText = joinErrorMsg
        )
        Button(
            onClick = { checkJoin() },
            colors = buttonColors(),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Join Group",
                fontWeight = FontWeight.SemiBold,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ){
            HorizontalDivider(
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.4f)
            )
            Text(
                text = "OR",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(1f/3f)
            )
            HorizontalDivider(
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(10.dp))
        TitleText("Create a new group!")
        InfoField(
            value = name,
            onValueChange = { name = it },
            labelText = "Name",
            isError = nameError,
            errorText = nameErrorMsg
        )
        InfoField(
            value = description,
            onValueChange = { description = it },
            labelText = "Description (optional)",
            isError = false,
            errorText = "Something went wrong.",
            singleLine = false,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
        )
        TertiaryButton(
            onClick = { createGroup() },
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Create Group",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun generateGroupCode(): String {
    var retStr = ""
    val charPool = ((0..9).asSequence() + ('a'..'z').asSequence() + ('A'..'Z').asSequence()).toList()
    for (i in (1..6)){
        retStr = "$retStr${charPool.random()}"
    }
    return retStr
}

private suspend fun doesGroupExist(code: String): Boolean {
    return supabase.from("groups")
        .select {
            filter {
                eq("id", code)
            }
        }
        .decodeSingleOrNull<Group>() != null
}