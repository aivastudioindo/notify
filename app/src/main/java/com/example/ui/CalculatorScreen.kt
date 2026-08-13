package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary

@Composable
fun CalculatorScreen(
    onUnlockWithPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var unlockMessage by remember { mutableStateOf<String?>(null) }

    fun processInput(btn: String) {
        unlockMessage = null
        when (btn) {
            "AC" -> {
                displayValue = "0"
                expression = ""
                isError = false
            }
            "⌫" -> {
                if (displayValue.length > 1) {
                    displayValue = displayValue.substring(0, displayValue.length - 1)
                } else {
                    displayValue = "0"
                }
            }
            "+/-" -> {
                if (displayValue != "0") {
                    displayValue = if (displayValue.startsWith("-")) {
                        displayValue.substring(1)
                    } else {
                        "-$displayValue"
                    }
                }
            }
            "%" -> {
                try {
                    val num = displayValue.toDouble()
                    displayValue = (num / 100.0).toString().removeSuffix(".0")
                } catch (e: Exception) {
                    isError = true
                }
            }
            "+", "-", "×", "÷" -> {
                expression = "$displayValue $btn "
                displayValue = "0"
            }
            "=" -> {
                // Secret Vault Check FIRST before calculator evaluation
                val cleanPinCandidate = displayValue.trim()
                if (cleanPinCandidate.isNotEmpty() && onUnlockWithPin(cleanPinCandidate)) {
                    unlockMessage = "🎉 PIN Benar! Membuka Famly..."
                    return
                }

                // If not PIN, do real calculator evaluation
                if (expression.isNotEmpty()) {
                    val fullExpr = "$expression$displayValue"
                    val parts = fullExpr.split(" ")
                    if (parts.size == 3) {
                        val num1 = parts[0].toDoubleOrNull()
                        val op = parts[1]
                        val num2 = parts[2].toDoubleOrNull()

                        if (num1 != null && num2 != null) {
                            val result = when (op) {
                                "+" -> num1 + num2
                                "-" -> num1 - num2
                                "×" -> num1 * num2
                                "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                                else -> Double.NaN
                            }
                            if (result.isNaN()) {
                                displayValue = "Error"
                                isError = true
                            } else {
                                displayValue = if (result % 1.0 == 0.0) {
                                    result.toLong().toString()
                                } else {
                                    String.format("%.4f", result).trimEnd('0').trimEnd('.')
                                }
                                expression = ""
                            }
                        }
                    }
                }
            }
            else -> { // Numbers and Decimal point
                if (displayValue == "0" || isError) {
                    displayValue = btn
                    isError = false
                } else if (displayValue.length < 12) {
                    displayValue += btn
                }

                // Auto unlock check if typed sequence equals PIN directly
                if (displayValue.length >= 4 && onUnlockWithPin(displayValue)) {
                    unlockMessage = "🎉 PIN Benar! Membuka Famly..."
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kalkulator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MinimalTextMuted
            )

            IconButton(onClick = { showHint = !showHint }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Petunjuk Penyamaran",
                    tint = MinimalTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Stealth Hint Banner
        AnimatedVisibility(visible = showHint, enter = fadeIn(), exit = fadeOut()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = MinimalLavenderPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mode Penyamaran Aktif: Ketik PIN 4-digit Anda lalu tekan tombol '=' untuk membuka Famly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextPrimary
                    )
                }
            }
        }

        // Display Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            if (unlockMessage != null) {
                Text(
                    text = unlockMessage!!,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF25D366),
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = expression,
                style = MaterialTheme.typography.bodyLarge,
                color = MinimalTextMuted,
                textAlign = TextAlign.End,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayValue,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MinimalTextPrimary,
                textAlign = TextAlign.End,
                maxLines = 1,
                fontSize = if (displayValue.length > 8) 36.sp else 48.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculator Keypad Grid
        val buttons = listOf(
            listOf("AC", "+/-", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { label ->
                        CalcButton(
                            label = label,
                            onClick = { processInput(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOperator = label in listOf("÷", "×", "-", "+", "=")
    val isAction = label in listOf("AC", "+/-", "%", "⌫")

    val containerColor = when {
        label == "=" -> MinimalLavenderPrimary
        isOperator -> Color(0xFF381E72)
        isAction -> MinimalCardBackground
        else -> MinimalSurfaceElevated
    }

    val textColor = when {
        label == "=" -> Color(0xFF381E72)
        isOperator -> MinimalLavenderPrimary
        isAction -> Color(0xFF38BDF8)
        else -> MinimalTextPrimary
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
