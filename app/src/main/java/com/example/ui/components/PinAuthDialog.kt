package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.ui.PinDialogMode
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun PinAuthDialog(
    mode: PinDialogMode,
    onDismiss: () -> Unit,
    onPinSubmit: (String) -> Boolean,
    onSetNewPin: (String) -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
            border = BorderStroke(1.dp, MinimalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    color = MinimalCardBackground,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MinimalBorder),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (mode == PinDialogMode.UNLOCK) Icons.Default.Lock else Icons.Default.Shield,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = when {
                        mode == PinDialogMode.UNLOCK -> "Buka Brankas Notifikasi"
                        isConfirmStep -> "Konfirmasi PIN Baru"
                        else -> "Buat PIN Brankas"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MinimalTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        mode == PinDialogMode.UNLOCK -> "Masukkan 4 digit PIN untuk melihat pesan terenkripsi"
                        isConfirmStep -> "Ulangi 4 digit PIN yang sama"
                        else -> "PIN ini digunakan untuk mengamankan data lokal Anda"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MinimalTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Indicator Dots
                val currentPinLength = if (isConfirmStep) confirmPin.length else enteredPin.length
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < currentPinLength
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MinimalLavenderPrimary else MinimalCardBackground
                                )
                                .then(
                                    if (!isFilled) Modifier.background(Color.Transparent) else Modifier
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error Message if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MinimalRoseText,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad 1-9, 0, Backspace
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (key in row) {
                                when (key) {
                                    "C" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    errorMessage = null
                                                    if (isConfirmStep) {
                                                        confirmPin = ""
                                                    } else {
                                                        enteredPin = ""
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "C",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MinimalTextMuted
                                            )
                                        }
                                    }
                                    "DEL" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    errorMessage = null
                                                    if (isConfirmStep) {
                                                        if (confirmPin.isNotEmpty()) {
                                                            confirmPin = confirmPin.dropLast(1)
                                                        }
                                                    } else {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Hapus",
                                                tint = MinimalTextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(MinimalCardBackground)
                                                .clickable {
                                                    errorMessage = null
                                                    if (mode == PinDialogMode.UNLOCK) {
                                                        if (enteredPin.length < 4) {
                                                            val newPin = enteredPin + key
                                                            enteredPin = newPin
                                                            if (newPin.length == 4) {
                                                                val success = onPinSubmit(newPin)
                                                                if (!success) {
                                                                    errorMessage = "PIN salah, silakan coba lagi"
                                                                    enteredPin = ""
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        // SET_NEW Mode
                                                        if (!isConfirmStep) {
                                                            if (enteredPin.length < 4) {
                                                                val newPin = enteredPin + key
                                                                enteredPin = newPin
                                                                if (newPin.length == 4) {
                                                                    isConfirmStep = true
                                                                }
                                                            }
                                                        } else {
                                                            if (confirmPin.length < 4) {
                                                                val newConfirm = confirmPin + key
                                                                confirmPin = newConfirm
                                                                if (newConfirm.length == 4) {
                                                                    if (confirmPin == enteredPin) {
                                                                        onSetNewPin(confirmPin)
                                                                    } else {
                                                                        errorMessage = "PIN tidak cocok, ulangi dari awal"
                                                                        enteredPin = ""
                                                                        confirmPin = ""
                                                                        isConfirmStep = false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MinimalTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.labelLarge,
                        color = MinimalTextSecondary
                    )
                }
            }
        }
    }
}
