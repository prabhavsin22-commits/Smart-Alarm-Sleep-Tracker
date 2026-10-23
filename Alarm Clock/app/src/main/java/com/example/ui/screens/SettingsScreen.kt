package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoundCatalog
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import com.example.ui.viewmodel.AlarmViewModel

@Composable
fun SettingsScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val healthStatus by viewModel.healthSyncStatus.collectAsState()
    val previewingSound by viewModel.previewingSound.collectAsState()
    val customTones by viewModel.customTones.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importCustomAudio(uri)
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedUri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (pickedUri != null) {
                val ringtone = RingtoneManager.getRingtone(context, pickedUri)
                val title = ringtone?.getTitle(context) ?: "Device Ringtone"
                viewModel.registerSystemRingtone(pickedUri, title)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF22263A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SunriseGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Customization & Accessibility",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Dark themes, sensory comfort & integrations",
                        fontSize = 12.sp,
                        color = Color(0xFFA5ABC7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Theme Palette Selector
        item {
            Text(
                text = "Dark Theme Style",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeCard(
                    title = "Deep Indigo",
                    subtitle = "Cosmic space",
                    color = Color(0xFF0F111E),
                    accent = SunriseGold,
                    isSelected = themeMode == AppThemeMode.DEEP_INDIGO,
                    onClick = { viewModel.setThemeMode(AppThemeMode.DEEP_INDIGO) },
                    modifier = Modifier.weight(1f)
                )
                ThemeCard(
                    title = "AMOLED Black",
                    subtitle = "Pure black",
                    color = Color.Black,
                    accent = SunriseBrightSun,
                    isSelected = themeMode == AppThemeMode.AMOLED_BLACK,
                    onClick = { viewModel.setThemeMode(AppThemeMode.AMOLED_BLACK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeCard(
                    title = "Bedside Red",
                    subtitle = "No blue light",
                    color = Color(0xFF140505),
                    accent = Color(0xFFFF5252),
                    isSelected = themeMode == AppThemeMode.BEDSIDE_REDSHIFT,
                    onClick = { viewModel.setThemeMode(AppThemeMode.BEDSIDE_REDSHIFT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Accessibility Settings
        item {
            Text(
                text = "Accessibility & Visibility",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141728))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // High Contrast Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High Contrast Text & Outlines",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Increases luminance ratio for easy viewing without squinting",
                                fontSize = 11.sp,
                                color = Color(0xFFA5ABC7)
                            )
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = { viewModel.toggleHighContrast(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SunriseGold,
                                checkedTrackColor = SunriseWarmOrange
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Scaling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Text & Clock Scale",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "${(fontScale * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunriseGold
                        )
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.setFontScale(it) },
                        valueRange = 0.9f..1.35f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = SunriseGold,
                            activeTrackColor = SunriseWarmOrange,
                            inactiveTrackColor = Color(0xFF262A42)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Lock Screen & Home Screen Widget Guide & Preview
        item {
            Text(
                text = "Lock Screen & Home Widget",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Glanceable status & one-tap 20m power nap launcher",
                fontSize = 11.sp,
                color = Color(0xFFA5ABC7)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_preview_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E30)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2E46))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Widget",
                                tint = SunriseGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AuraRise Status (Widget Preview)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunriseGold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2E1C40), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Sleep 91%", fontSize = 10.sp, color = SleepRemLavender)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "06:30 AM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Ambient Sunrise (15m) • Math Task",
                        fontSize = 11.sp,
                        color = Color(0xFFA0A3BD)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SunriseWarmOrange, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "⚡ 20m Nap",
                                color = Color(0xFF12131C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Supported on Home & Lock Screen (Keyguard)",
                            fontSize = 10.sp,
                            color = Color(0xFF8E93B0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Alarm Audio Library (Device Tones, Loud Alerts & Nature Soundscapes)
        item {
            Column {
                Text(
                    text = "Alarm Audio Library",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Play and preview your own audio files, system tones, loud sirens, and ambient soundscapes.",
                    fontSize = 11.sp,
                    color = Color(0xFFA0A5C2)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section: Your Device Tones & Files
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 Your Device Tones & Files",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunriseGold
                    )
                    Text(
                        text = "${customTones.size} imported",
                        fontSize = 11.sp,
                        color = Color(0xFFA0A5C2)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("settings_pick_audio_file"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SunriseBrightSun),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3E1B))
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Audio File", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Device Tone")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("settings_pick_ringtone"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3845))
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Ringtone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Display custom tones
        items(customTones.size) { index ->
            val tone = customTones[index]
            val isPlaying = previewingSound == tone.displayName

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPlaying) Color(0xFF2E2614) else Color(0xFF161928)
                ),
                border = if (isPlaying) {
                    androidx.compose.foundation.BorderStroke(1.dp, SunriseGold)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tone.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF383216), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (tone.isSystemRingtone) "SYSTEM" else "FILE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SunriseGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (tone.isSystemRingtone) "Device built-in alarm / ringtone" else "Imported from device storage",
                            fontSize = 11.sp,
                            color = Color(0xFFA0A5C2)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.previewSound(tone.displayName, tone.uriString) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = if (isPlaying) Color(0xFFFF5252) else SunriseGold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.removeCustomTone(tone.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF6B728E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section header for built-in catalog
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Curated Loud Tones & Nature Soundscapes",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(SoundCatalog.soundscapes.size) { index ->
            val sound = SoundCatalog.soundscapes[index]
            val isPlaying = previewingSound == sound.name

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPlaying) {
                        if (sound.isLoud) Color(0xFF2E1317) else Color(0xFF1B2338)
                    } else {
                        if (sound.isLoud) Color(0xFF1A1318) else Color(0xFF141728)
                    }
                ),
                border = if (isPlaying) {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (sound.isLoud) Color(0xFFFF5252) else SunriseGold
                    )
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sound.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (sound.isLoud) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3D161B), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🚨 LOUD ALERT",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF22263C), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sound.category,
                                        fontSize = 9.sp,
                                        color = AlertCyan
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sound.description,
                            fontSize = 11.sp,
                            color = Color(0xFFA0A5C2)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.previewSound(sound.name) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                            tint = if (isPlaying) Color(0xFFFF5252) else if (sound.isLoud) Color(0xFFFF7A7A) else SunriseGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    title: String,
    subtitle: String,
    color: Color,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161928)),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(accent)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 9.sp, color = Color(0xFF8E93B0))
        }
    }
}
