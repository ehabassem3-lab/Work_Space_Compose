package com.example.workspace.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workspace.R
import com.example.workspace.viewmodel.CreateTaskViewModel
import com.example.workspace.model.PriorityLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onDismiss: () -> Unit,
    onNavigateToDrafts: () -> Unit, // Add this line
    viewModel: CreateTaskViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // --- Theme Colors ---
    val containerColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val inputBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    // --- Local State ---
    var projectExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    val projectsList = listOf("Mobile App", "Website Redesign", "Marketing")

    // Real Members
    val assigneesList by viewModel.availableAssignees.collectAsState(initial = emptyList())

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // --- Date Picker ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },

            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        viewModel.onDateChange(formatter.format(Date(selectedMillis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        containerColor = containerColor,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = textColor) }
                Text(if (state.isEditMode) "Edit Task" else stringResource(R.string.create_new_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor)
// Inside CreateTaskScreen.kt topBar
                TextButton(onClick = {
                    viewModel.saveAsDraft {
                        onNavigateToDrafts()
                        // Logic to navigate to Drafts screen or show message
                        Toast.makeText(context, "Draft Saved", Toast.LENGTH_SHORT).show()
                    }
                }
                ) {
                    Text(stringResource(R.string.action_drafts), color = primaryColor, fontWeight = FontWeight.Bold)
                }            }
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveTask(
                        onSuccess = {
                            val msg = if (state.isEditMode) "Task Updated" else context.getString(R.string.msg_task_created)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onError = {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (state.isEditMode) "Update Task" else stringResource(R.string.btn_create_task), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {

            // --- Task Name ---
            LabelText(stringResource(R.string.label_task_name), labelColor)
            CustomTextField(
                value = state.title,
                onValueChange = viewModel::onNameChange,
                placeholder = stringResource(R.string.hint_task_name),
                singleLine = true,
                bgColor = inputBgColor,
                textColor = textColor,
                isError = state.isTitleError,
                errorColor = errorColor,
                modifier = Modifier.shake(state.isTitleError)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- Description ---
            LabelText(stringResource(R.string.label_description), labelColor)
            CustomTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = stringResource(R.string.hint_description),
                singleLine = false,
                modifier = Modifier.height(120.dp).shake(state.isDescError),
                bgColor = inputBgColor,
                textColor = textColor,
                isError = state.isDescError,
                errorColor = errorColor
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- Project ---
            Box {
                SelectionCard(
                    icon = Icons.Default.Folder, iconBg = primaryColor.copy(0.1f), iconTint = primaryColor,
                    title = stringResource(R.string.label_project),
                    value = state.projectName.ifEmpty { "Select Project" },
                    bgColor = inputBgColor, textColor = textColor, labelColor = labelColor,
                    onClick = { projectExpanded = true },
                    isError = state.isProjectError, errorColor = errorColor,
                    modifier = Modifier.shake(state.isProjectError)
                )
                DropdownMenu(expanded = projectExpanded, onDismissRequest = { projectExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    projectsList.forEach { project ->
                        DropdownMenuItem(text = { Text(project) }, onClick = { viewModel.onProjectChange(project); projectExpanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Assignee ---
            Box {
                SelectionCard(
                    icon = Icons.Default.Person, iconBg = MaterialTheme.colorScheme.surfaceVariant, iconTint = labelColor,
                    title = stringResource(R.string.label_assignee),
                    value = state.assignee.ifEmpty { stringResource(R.string.hint_select_assignee) },
                    bgColor = inputBgColor, textColor = textColor, labelColor = labelColor, isAddButton = true,
                    onClick = { assigneeExpanded = true },
                    isError = state.isAssigneeError, errorColor = errorColor,
                    modifier = Modifier.shake(state.isAssigneeError)
                )
                DropdownMenu(expanded = assigneeExpanded, onDismissRequest = { assigneeExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    if (assigneesList.isEmpty()) {
                        DropdownMenuItem(text = { Text("No members", color = Color.Gray) }, onClick = { assigneeExpanded = false })
                    } else {
                        assigneesList.forEach { person ->
                            DropdownMenuItem(text = { Text(person) }, onClick = { viewModel.onAssigneeChange(person); assigneeExpanded = false })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Date & Priority ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    SelectionCardSmall(
                        icon = Icons.Outlined.CalendarToday, label = stringResource(R.string.label_due_date),
                        value = state.date.ifEmpty { "Select Date" },
                        bgColor = inputBgColor, textColor = textColor, labelColor = labelColor, iconTint = labelColor,
                        onClick = { showDatePicker = true },
                        isError = state.isDateError, errorColor = errorColor,
                        modifier = Modifier.shake(state.isDateError)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SelectionCardSmall(
                        icon = Icons.Outlined.Flag, label = stringResource(R.string.label_priority),
                        value = stringResource(state.priority.labelResId),
                        bgColor = inputBgColor, textColor = textColor, labelColor = labelColor, iconTint = Color(0xFFEF4444),
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Priority Level ---
            LabelText(stringResource(R.string.label_priority_level), labelColor)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriorityLevel.values().forEach { level ->
                    PriorityChip(
                        label = stringResource(level.labelResId),
                        selected = state.priority == level,
                        onClick = { viewModel.onPriorityChange(level) },
                        activeColor = primaryColor,
                        inactiveColor = inputBgColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ================== UI Components ==================

@Composable
fun LabelText(text: String, color: Color) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun CustomTextField(
    value: String, onValueChange: (String) -> Unit, placeholder: String, singleLine: Boolean,
    bgColor: Color, textColor: Color, modifier: Modifier = Modifier,
    isError: Boolean = false, errorColor: Color = Color.Red
) {
    TextField(
        value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, color = textColor.copy(0.5f)) },
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(if (isError) 1.dp else 0.dp, if (isError) errorColor else Color.Transparent, RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(focusedContainerColor = bgColor, unfocusedContainerColor = bgColor, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary, focusedTextColor = textColor, unfocusedTextColor = textColor),
        singleLine = singleLine, shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SelectionCard(
    icon: ImageVector, iconBg: Color, iconTint: Color, title: String, value: String,
    bgColor: Color, textColor: Color, labelColor: Color, isAddButton: Boolean = false, onClick: () -> Unit,
    isError: Boolean = false, errorColor: Color = Color.Red,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isError) 1.dp else 0.dp, if (isError) errorColor else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.SemiBold)
        }
        Icon(if (isAddButton) Icons.Default.Add else Icons.Default.KeyboardArrowDown, null, tint = labelColor)
    }
}

@Composable
fun SelectionCardSmall(
    icon: ImageVector, label: String, value: String,
    bgColor: Color, textColor: Color, labelColor: Color, iconTint: Color, onClick: () -> Unit,
    isError: Boolean = false, errorColor: Color = Color.Red,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isError) 1.dp else 0.dp, if (isError) errorColor else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 9.sp, color = labelColor, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PriorityChip(label: String, selected: Boolean, onClick: () -> Unit, activeColor: Color, inactiveColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(40.dp).clip(RoundedCornerShape(8.dp)).background(if (selected) MaterialTheme.colorScheme.surface else inactiveColor).border(1.dp, if (selected) activeColor else Color.Transparent, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) activeColor else textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
}

// ================== Animation Utils ==================

fun Modifier.shake(enabled: Boolean) = composed {
    val translationX = remember { Animatable(0f) }
    LaunchedEffect(enabled) {
        if (enabled) {
            translationX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -16f at 50
                    16f at 100
                    -16f at 150
                    0f at 200
                }
            )
        }
    }
    graphicsLayer { this.translationX = translationX.value }
}