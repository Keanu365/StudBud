package io.github.keanu365.studbud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keanu365.studbud.theme.buttonColors
import io.github.keanu365.studbud.theme.datePickerColors
import io.github.keanu365.studbud.theme.errorButtonColors
import io.github.keanu365.studbud.theme.outlinedTextFieldColors
import kotlinx.datetime.number
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_add
import studbud.composeapp.generated.resources.icon_arrow_dropdown
import studbud.composeapp.generated.resources.icon_error
import studbud.composeapp.generated.resources.icon_info
import studbud.composeapp.generated.resources.icon_invisible
import studbud.composeapp.generated.resources.icon_visible
import studbud.composeapp.generated.resources.icon_wand
import studbud.composeapp.generated.resources.icon_warning

@Composable
fun TitleText(text: String){
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(bottom = 15.dp)
            .fillMaxWidth()
    )
}

@Composable
fun InfoField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    leadingIconResource: DrawableResource? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean,
    errorText: String = "Error",
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text
){
    var showPassword by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()){
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(labelText) },
            leadingIcon = leadingIconResource?.let{
                {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
            singleLine = singleLine,
            readOnly = readOnly,
            isError = isError,
            colors = outlinedTextFieldColors(),
            shape = RoundedCornerShape(15.dp),
            modifier = modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(75.dp) else Modifier),
            //Password transformations
            trailingIcon =  {
                if (isPassword)
                    IconButton(
                        onClick = { showPassword = !showPassword },
                    ){
                        Icon(
                            painter = painterResource(
                                if (showPassword) Res.drawable.icon_visible
                                else Res.drawable.icon_invisible
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                else if (trailingIcon != null) trailingIcon()
            },
            visualTransformation = if (showPassword || !isPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization,
                autoCorrectEnabled = capitalization == KeyboardCapitalization.Sentences,
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = ImeAction.Next,
            )
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 5.dp)
        ) {
            if (isError) {
                Icon(
                    painter = painterResource(Res.drawable.icon_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AnimatedDropdown(
    show: Boolean,
    title: String,
    firstLabel: String = "Name",
    secondLabel: String = " ",
    dataList: List<Any>,
    onDataClicked: ((Any) -> Unit)? = null,
    onShowChanged: (Boolean) -> Unit = {}
){
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = {
                    onShowChanged(!show)
                },
            ){
                Icon(
                    painter = painterResource(Res.drawable.icon_arrow_dropdown),
                    contentDescription = null,
                    modifier = animateDropdown(show)
                )
            }
        }
        AnimatedVisibility(
            visible = show,
            enter = slideInVertically(initialOffsetY = { -40 }) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically(targetOffsetY = { -40 }) + shrinkVertically(
                shrinkTowards = Alignment.Top
            ) + fadeOut()
        ){
            DataView(
                dataList = dataList,
                firstLabel = firstLabel,
                secondLabel = secondLabel,
                onDataClicked = onDataClicked
            )
        }
    }
}

@Composable
fun AssignmentsDropdown(
    show: Boolean,
    onShowChanged: (Boolean) -> Unit,
    title: String,
    assignments: List<Assignment>,
    onAssignmentAdd: () -> Unit,
    onAssignmentClicked: (Assignment) -> Unit
){
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Row{
                IconButton(
                    onClick = onAssignmentAdd
                ){
                    Icon(
                        painter = painterResource(Res.drawable.icon_add),
                        contentDescription = null,
                        modifier = animateDropdown(show)
                    )
                }
                IconButton(
                    onClick = {
                        onShowChanged(!show)
                    },
                ){
                    Icon(
                        painter = painterResource(Res.drawable.icon_arrow_dropdown),
                        contentDescription = null,
                        modifier = animateDropdown(show)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = show,
            enter = slideInVertically(initialOffsetY = { -40 }) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically(targetOffsetY = { -40 }) + shrinkVertically(
                shrinkTowards = Alignment.Top
            ) + fadeOut()
        ){
            DataView(
                dataList = assignments,
                secondLabel = "Due Date",
                onDataClicked = { onAssignmentClicked(it as Assignment) }
            )
        }
    }
}

@Composable
fun DataView(
    dataList: List<Any>,
    firstLabel: String = "Name",
    secondLabel: String = " ",
    onDataClicked: ((Any) -> Unit)? = null
){
    Column(Modifier.padding(vertical = 10.dp)){
        if (dataList.isEmpty()) Text(
            text = "Nothing to see here yet!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) else Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 5.dp)
        ){
            Text(
                text = firstLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = secondLabel,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
        dataList.forEachIndexed { index, data ->
            var firstText = "$data"
            var secondText = ""
            when (data) {
                is User -> {
                    firstText = data.username
                }
                is Group -> {
                    firstText = data.name
                    secondText = "${data.members.size}"
                }
                is Assignment -> {
                    firstText = data.name
                    secondText = "${data.due_date.day}/${data.due_date.month.number}/${data.due_date.year}"
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (index % 2 == 0) Color.Transparent
                        else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .then(
                        if (onDataClicked != null) Modifier.clickable { onDataClicked(data) }
                        else Modifier
                    )
            ){
                Text(
                    text = firstText,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .fillMaxWidth(0.6f)
                        .horizontalScroll(rememberScrollState()) //In case name is too long
                )
                Text(
                    text = secondText,
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(horizontal = 5.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun animateDropdown(
    show: Boolean
): Modifier {
    return Modifier.rotate(
        animateFloatAsState(
            targetValue = if (show) 180f else 0f,
            label = "Dropdown Animation"
        ).value
    )
}

@Composable
fun SuccessPage(
    title: String,
    image: @Composable () -> Unit,
    content: @Composable () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 15.dp)
                    .fillMaxWidth()
            )
            image()
            Spacer(modifier = Modifier.height(15.dp))
            content()
            Spacer(modifier = Modifier.height(50.dp))
        }
        TertiaryButton(
            onClick = onReturn
        ){
            Text(
                text = "Return to home",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            )
        }
    }
}

@Composable
fun TertiaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
){
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = buttonColors(),
        content = content
    )
}

@Composable
fun ErrorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
){
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = errorButtonColors(),
        content = content
    )
}

@Composable
fun SurfaceAlert(
    alertType: AlertType,
    message: String,
    modifier: Modifier = Modifier,
){
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ){
            Icon(
                painter = painterResource(
                    when(alertType){
                        AlertType.SUGGESTION -> Res.drawable.icon_wand
                        AlertType.INFO -> Res.drawable.icon_info
                        AlertType.WARNING -> Res.drawable.icon_warning
                        AlertType.ERROR -> Res.drawable.icon_error
                    }
                ),
                contentDescription = null,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 15.dp)
            )
        }
    }
}

enum class AlertType{
    SUGGESTION, INFO, WARNING, ERROR
}

@Composable
fun DatePickerModal(
    title: String,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TertiaryButton(
                onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    ) {
        DatePicker(
            state = datePickerState,
            title = { Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) },
            colors = datePickerColors(),
        )
    }
}