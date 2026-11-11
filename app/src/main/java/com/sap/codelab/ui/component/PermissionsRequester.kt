package com.sap.codelab.ui.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sap.codelab.MainActivity
import com.sap.codelab.R
import com.sap.codelab.util.hasBackgroundLocationPermission
import com.sap.codelab.util.hasForegroundLocationPermission
import com.sap.codelab.util.hasNotificationPermission

@Composable
fun PermissionRequester(
    fineLocationRequestAllowed: Boolean,
    onFineLocationRequestSuppress: () -> Unit,
    backgroundLocationRequestAllowed: Boolean,
    onBackgroundLocationRequestSuppress: () -> Unit,
    notificationRequestAllowed: Boolean,
    onNotificationRequestSuppress: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as MainActivity
    var hasFineLocationPermission by remember {
        mutableStateOf(context.hasForegroundLocationPermission())
    }
    var pendingFineLocationPermissionGrant by rememberSaveable { mutableStateOf(false) }
    var hasBackgroundLocationPermission by remember {
        mutableStateOf(context.hasBackgroundLocationPermission())
    }
    var pendingBackgroundLocationPermissionGrant by rememberSaveable { mutableStateOf(false) }
    var hasNotificationsPermission by remember {
        mutableStateOf(context.hasNotificationPermission())
    }
    var pendingNotificationPermissionGrant by rememberSaveable { mutableStateOf(false) }
    val foregroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasFineLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        pendingFineLocationPermissionGrant = false
    }
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasBackgroundLocationPermission = isGranted
        pendingBackgroundLocationPermissionGrant = false
    }
    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasNotificationsPermission = isGranted
        pendingNotificationPermissionGrant = false
    }

    when {
        !pendingFineLocationPermissionGrant &&
                !hasFineLocationPermission &&
                fineLocationRequestAllowed -> {

            if (activity.shouldShowRequestPermissionRationale(FINE_LOCATION_PERMISSION)) {
                RequestDialog(
                    title = stringResource(id = R.string.foreground_location),
                    message = stringResource(id = R.string.foreground_location_rationale),
                    onConfirm = {
                        pendingFineLocationPermissionGrant = true
                        foregroundLocationPermissionLauncher.launch(foregroundLocationPermissions)
                    },
                    onDeny = onFineLocationRequestSuppress,
                )
            } else {
                LaunchedEffect(Unit) {
                    pendingFineLocationPermissionGrant = true
                    foregroundLocationPermissionLauncher.launch(foregroundLocationPermissions)
                }
            }
        }

        !pendingBackgroundLocationPermissionGrant &&
                !hasBackgroundLocationPermission &&
                backgroundLocationRequestAllowed &&
                activity.shouldShowRequestPermissionRationale(BACKGROUND_LOCATION_PERMISSION) ->
            RequestDialog(
                title = stringResource(id = R.string.background_location),
                message = stringResource(id = R.string.background_location_rationale),
                onConfirm = {
                    pendingBackgroundLocationPermissionGrant = true
                    backgroundLocationPermissionLauncher.launch(BACKGROUND_LOCATION_PERMISSION)
                },
                onDeny = onBackgroundLocationRequestSuppress,
            )

        !pendingNotificationPermissionGrant &&
                !hasNotificationsPermission &&
                notificationRequestAllowed -> {
            if (activity.shouldShowRequestPermissionRationale(NOTIFICATION_PERMISSION)) {
                RequestDialog(
                    title = stringResource(id = R.string.notifications),
                    message = stringResource(id = R.string.notifications_rationale),
                    onConfirm = {
                        pendingNotificationPermissionGrant = true
                        notificationsPermissionLauncher.launch(NOTIFICATION_PERMISSION)
                    },
                    onDeny = onNotificationRequestSuppress,
                )
            } else {
                LaunchedEffect(Unit) {
                    pendingNotificationPermissionGrant = true
                    notificationsPermissionLauncher.launch(NOTIFICATION_PERMISSION)
                }
            }
        }
    }
}

@Composable
private fun RequestDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.allow_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(text = stringResource(id = R.string.deny_permission))
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
    )
}

private val foregroundLocationPermissions = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)
private const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
private const val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
