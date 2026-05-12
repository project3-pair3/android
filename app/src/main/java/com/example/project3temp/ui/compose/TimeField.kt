package com.example.project3temp.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 영업 시간(시:분) 입력 보관
internal data class TimeInput(
    val hour: Int? = null,
    val minute: Int? = null,
) {
    fun isComplete(): Boolean = hour != null && minute != null

    // 서버로 보낼 "HH:mm:ss" 문자열 (초는 항상 00)
    fun toApiString(): String? {
        if (!isComplete()) return null
        return "%02d:%02d:00".format(hour, minute)
    }

    // 화면 표시용 "HH:mm"
    fun displayString(): String {
        if (!isComplete()) return ""
        return "%02d:%02d".format(hour, minute)
    }
}

// 시:분 입력 필드. 탭하면 TimePicker 다이얼로그가 뜸
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimeField(
    label: String,
    value: TimeInput,
    onChange: (TimeInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2D9CF), RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { showTimePicker = true }
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.displayString().ifBlank { "HH:mm" },
                fontSize = 14.sp,
                color = if (value.isComplete()) Color.Black else Color.Gray,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = value.hour ?: 9,
            initialMinute = value.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("$label 시간 선택") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = state)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(
                        TimeInput(
                            hour = state.hour,
                            minute = state.minute,
                        ),
                    )
                    showTimePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            },
        )
    }
}
