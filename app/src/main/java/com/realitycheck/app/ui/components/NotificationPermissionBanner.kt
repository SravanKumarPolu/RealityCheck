package com.realitycheck.app.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.ui.theme.Spacing
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import com.realitycheck.app.ui.viewmodel.NotificationPermissionState

/**
 * Banner that shows notification permission request UI
 * Only shows if permission is not granted
 */
@Composable
fun NotificationPermissionBanner(
    viewModel: DecisionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val permissionState by viewModel.notificationPermissionState.collectAsState()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermissionState(isGranted)
    }
    
    // Only show banner if permission is denied or needs exact alarm
    if (permissionState is NotificationPermissionState.Denied || 
        permissionState is NotificationPermissionState.NeedsExactAlarm ||
        permissionState is NotificationPermissionState.Unknown) {
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationImportant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = when (permissionState) {
                            is NotificationPermissionState.Denied -> 
                                "Get reminded when it's time to check in on your decisions"
                            is NotificationPermissionState.NeedsExactAlarm ->
                                "Enable exact alarms for accurate reminders"
                            else -> 
                                "Enable notifications to get reminders"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.updateNotificationPermissionState(true)
                        }
                    }
                ) {
                    Text(
                        text = "Enable",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

