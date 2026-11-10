package com.sap.codelab.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.ui.screen.memo.MemoLaunchMode

@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateToMemo: (MemoLaunchMode) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onToggleMemosListMode = viewModel::toggleMemosListMode,
        onNavigateToMemo = { memoId ->
            val launchMode = if (memoId > 0) {
                MemoLaunchMode.View(memoId = memoId)
            } else {
                MemoLaunchMode.Create
            }

            onNavigateToMemo(launchMode)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleMemosListMode: () -> Unit,
    onNavigateToMemo: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    TextButton(onClick = onToggleMemosListMode) {
                        val stringRes = if (uiState.showsAllMemos) {
                            R.string.show_open
                        } else {
                            R.string.show_all
                        }

                        Text(text = stringResource(id = stringRes))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToMemo(DEFAULT_MEMO_ID) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(id = R.string.create_memo),
                )
            }
        },
    ) { innerPadding ->
        if (uiState.memos.isNotEmpty()) {
            LazyColumn(modifier = Modifier.padding(paddingValues = innerPadding)) {
                items(items = uiState.memos, key = { it.id }) { memo ->
                    // TODO: Memo
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.memo_list_empty))
                }
            }
        }
    }
}

/**
 * The ID to pass to the Memo screen when we're intending to create a new memo.
 */
private const val DEFAULT_MEMO_ID = -1L
