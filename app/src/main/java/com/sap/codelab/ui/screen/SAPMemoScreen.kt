package com.sap.codelab.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sap.codelab.ui.navigation.NavigationScreen
import com.sap.codelab.ui.screen.home.HomeScreen
import com.sap.codelab.ui.screen.memo.MemoScreen

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
                    navigateToMemo = { memoId ->
                        backStack.add(NavigationScreen.Memo(memoId))
                    },
                )
            }

            entry<NavigationScreen.Memo> {
                MemoScreen(navigateBack = backStack::removeLastOrNull)
            }
        },
    )
}
