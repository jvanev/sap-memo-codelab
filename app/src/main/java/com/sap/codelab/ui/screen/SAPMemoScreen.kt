package com.sap.codelab.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sap.codelab.ui.navigation.NavigationScreen
import com.sap.codelab.ui.screen.home.HomeScreen
import com.sap.codelab.ui.screen.memo.MemoScreen
import com.sap.codelab.ui.screen.memo.MemoViewModel

@Composable
fun SAPMemoScreen() {
    val backStack = rememberNavBackStack(NavigationScreen.Home)

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
