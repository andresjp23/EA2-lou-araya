package com.example.evaluacion2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text("Selecciona una fecha") },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Abrir calendario") },
            isError = error != null,
            supportingText = error?.let { message -> { Text(message) } }
        )
        Box(Modifier.matchParentSize().clickable { showDatePicker = true })
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value.toUtcDateMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onValueChange(it.toIsoDate()) }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Selecciona una fecha", Modifier.padding(16.dp)) }
            )
        }
    }
}

private fun String.toUtcDateMillis(): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(this)?.time
}.getOrNull()

private fun Long.toIsoDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(this)
