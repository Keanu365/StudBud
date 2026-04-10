package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.Achievement
import io.github.keanu365.studbud.TitleText
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_ach
import studbud.composeapp.generated.resources.icon_check
import studbud.composeapp.generated.resources.icon_lock

@Composable
fun AchievementsPage(
    userAchievements: List<Int>,
    allAchievements: List<Achievement>
){
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ){
        item {
            Spacer(Modifier.height(15.dp))
            TitleText("Achievements")
        }

        items(allAchievements.sortedBy { it.id }) { achievement ->
            AchievementCard(
                achievement = achievement,
                isAttained = userAchievements.contains(achievement.id),
                modifier = Modifier.fillMaxWidth().padding(5.dp)
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier,
    isAttained: Boolean = true
){
    Card(
        modifier = modifier
    ){
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)){
            Icon(
                painter = painterResource(
                    if (isAttained) Res.drawable.icon_ach
                    else Res.drawable.icon_lock
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .size(60.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.9f)){
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (achievement.secret && !isAttained) "???" else achievement.description,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isAttained){
                Icon(
                    painter = painterResource(Res.drawable.icon_check),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth()
                )
            }
        }
    }
}