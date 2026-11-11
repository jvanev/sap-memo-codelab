package com.sap.codelab.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sap.codelab.MainActivity
import com.sap.codelab.SAPMemo
import com.sap.codelab.ui.navigation.NavigationScreen
import com.sap.codelab.ui.screen.home.HomeScreen
import com.sap.codelab.ui.screen.memo.MemoLaunchMode
import com.sap.codelab.ui.screen.memo.MemoScreen
import com.sap.codelab.ui.screen.memo.MemoViewModel

@Composable
fun SAPMemoScreen() {
    val context = LocalContext.current
    val activity = context as MainActivity
    val backStack = rememberNavBackStack(NavigationScreen.Home)

    LaunchedEffect(Unit) {
        activity.intent?.addTriggeredGeofenceToBackStack(backStack)
    }
    DisposableEffect(activity) {
        val intentListener = Consumer<Intent> { intent ->
            intent.addTriggeredGeofenceToBackStack(backStack)
        }

        activity.addOnNewIntentListener(intentListener)
        onDispose {
            activity.removeOnNewIntentListener(intentListener)
        }
    }
    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<NavigationScreen.Home> {
                HomeScreen(
                    viewModel = viewModel(),
                    onNavigateToMemo = { launchMode ->
                        backStack.add(NavigationScreen.Memo(launchMode))
                    },
                )
            }

            entry<NavigationScreen.Memo> { destination ->
                MemoScreen(
                    viewModel = viewModel { MemoViewModel(launchMode = destination.launchMode) },
                    onNavigateBack = backStack::removeLastOrNull,
                )
            }
        },
    )
}

/**
 * Checks if the intent contains data about triggered geofence,
 * and adds the corresponding memo to the specified [backStack].
 */
private fun Intent.addTriggeredGeofenceToBackStack(backStack: NavBackStack<NavKey>) {
    val memoId = getLongExtra(SAPMemo.INTENT_EXTRA_MEMO_ID, -1)

    if (memoId > -1) {
        val memoKey = NavigationScreen.Memo(MemoLaunchMode.View(memoId))

        if (backStack.lastOrNull() != memoKey) {
            backStack.add(memoKey)
        }
    }

    removeExtra(SAPMemo.INTENT_EXTRA_MEMO_ID)
}
