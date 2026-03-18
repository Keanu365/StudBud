package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.account.InfoField
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.launch
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus

@Composable
fun AddGroupPage(
    user: User,
    onJoin: () -> Unit = {},
    showSnackBar: (String) -> Unit
){
    val networkStatus by rememberNetworkStatus()
    val coroutineScope = rememberCoroutineScope()
    var joinGroupCode by remember { mutableStateOf("") }
    var isJoinGroupCodeError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("Invalid Group Code") }
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
                errorMsg = "Invalid Group Code"
            }
            else if (group.members.contains(user.id)) {
                isJoinGroupCodeError = true
                errorMsg = "You're already a member of this group!"
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
                onJoin()
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Join A Group",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 15.dp)
                .fillMaxWidth()
        )
        InfoField(
            value = joinGroupCode,
            onValueChange = { joinGroupCode = it },
            labelText = "Group Code",
            isError = isJoinGroupCodeError,
            errorText = errorMsg
        )
        Button(
            onClick = { checkJoin() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "Join/Create a Group",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}