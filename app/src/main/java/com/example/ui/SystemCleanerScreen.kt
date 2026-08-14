package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalEmerald
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRose
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SystemCleanerScreen(
    onUnlockWithPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isCleaning by remember { mutableStateOf(false) }
    var cleanProgress by remember { mutableStateOf(0f) }
    var ramUsedPercent by remember { mutableStateOf(78) }
    var cacheAmountMb by remember { mutableStateOf(2450) }
    var isCleanedSuccess by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var unlockMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val animatedProgress by animateFloatAsState(
        targetValue = if (isCleaning) cleanProgress else (ramUsedPercent / 100f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "clean_progress"
    )

    fun startCleaningProcess() {
        if (isCleaning) return
        isCleaning = true
        isCleanedSuccess = false
        cleanProgress = 0f
        scope.launch {
            for (i in 1..10) {
                delay(180)
                cleanProgress = i / 10f
            }
            delay(300)
            ramUsedPercent = 38
            cacheAmountMb = 120
            isCleaning = false
            isCleanedSuccess = true
        }
    }

    fun submitPinCheck() {
        val cleanPin = pinInput.trim()
        if (cleanPin.isNotEmpty() && onUnlockWithPin(cleanPin)) {
            pinError = false
            showPinDialog = false
            pinInput = ""
            unlockMessage = "🎉 PIN Benar! Membuka Famly..."
        } else {
            pinError = true
            unlockMessage = "❌ Kode PIN tidak valid"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MinimalLavenderPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pembersih Sistem Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Optimasi RAM & Pembersih Chace",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalTextMuted
                        )
                    }
                }
            }
        }

        // Status Message Unlock Toast
        item {
            unlockMessage?.let { msg ->
                Surface(
                    color = if (pinError) MinimalRose.copy(alpha = 0.15f) else MinimalEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (pinError) MinimalRose else MinimalEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (pinError) MinimalRoseText else MinimalEmerald,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // RAM & System Cleaner Gauge Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (ramUsedPercent > 70) MinimalRose else MinimalEmerald,
                            strokeWidth = 10.dp,
                            trackColor = MinimalSurfaceElevated
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isCleaning) "${(cleanProgress * 100).toInt()}%" else "$ramUsedPercent%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = if (isCleaning) "Proses..." else "RAM Terpakai",
                                style = MaterialTheme.typography.labelMedium,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isCleanedSuccess) "✨ RAM Berhasil Dioptimalkan!" else "Terdapat ${(cacheAmountMb / 1024f).let { String.format("%.1f", it) }} GB berkas sampah",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCleanedSuccess) MinimalEmerald else MinimalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = if (isCleaning) MinimalLavenderPrimary.copy(alpha = 0.5f) else MinimalLavenderPrimary,
                        contentColor = Color(0xFF381E72),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pointerInput(isCleaning) {
                                if (isCleaning) return@pointerInput
                                detectTapGestures(
                                    onTap = {
                                        startCleaningProcess()
                                    },
                                    onPress = {
                                        val job = scope.launch {
                                            delay(5000L)
                                            showPinDialog = true
                                        }
                                        try {
                                            awaitRelease()
                                        } finally {
                                            job.cancel()
                                        }
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCleaning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF381E72),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Membersihkan...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("BERSIHKAN RAM SEKARANG", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick System Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric 1: CPU Temp
                Card(
                    colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                    border = BorderStroke(1.dp, MinimalBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = MinimalEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Suhu CPU", style = MaterialTheme.typography.labelSmall, color = MinimalTextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("36.5 °C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MinimalTextPrimary)
                        Text("Status Normal", style = MaterialTheme.typography.labelSmall, color = MinimalEmerald)
                    }
                }

                // Metric 2: Storage
                Card(
                    colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                    border = BorderStroke(1.dp, MinimalBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MinimalLavenderPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Penyimpanan", style = MaterialTheme.typography.labelSmall, color = MinimalTextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("42.8 / 64 GB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MinimalTextPrimary)
                        Text("66% Digunakan", style = MaterialTheme.typography.labelSmall, color = MinimalTextSecondary)
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MinimalLavenderPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verifikasi Keamanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MinimalTextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan PIN rahasia Anda untuk membuka aplikasi Famly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { newValue ->
                            if (newValue.length <= 8) {
                                pinInput = newValue
                                if (newValue.length >= 4 && onUnlockWithPin(newValue.trim())) {
                                    pinError = false
                                    showPinDialog = false
                                    pinInput = ""
                                    unlockMessage = "🎉 PIN Benar! Membuka Famly..."
                                }
                            }
                        },
                        placeholder = { Text("Ketik PIN...", style = MaterialTheme.typography.bodyMedium, color = MinimalTextMuted) },
                        singleLine = true,
                        isError = pinError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            submitPinCheck()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MinimalSurfaceElevated,
                            unfocusedContainerColor = MinimalSurfaceElevated,
                            focusedBorderColor = MinimalLavenderPrimary,
                            unfocusedBorderColor = MinimalBorder,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kode PIN tidak valid",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalRose
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        submitPinCheck()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalLavenderPrimary,
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buka", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinInput = ""
                        pinError = false
                    }
                ) {
                    Text("Batal", color = MinimalTextMuted)
                }
            },
            containerColor = MinimalCardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
