package com.sap.codelab.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.component.SAPMemoTopAppBar
import com.sap.codelab.ui.screen.memo.MemoLaunchMode
import com.sap.codelab.ui.theme.LocalDimens
import com.sap.codelab.ui.theme.sapTopAppBarTextButtonColors

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

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleMemosListMode: () -> Unit,
    onNavigateToMemo: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            SAPMemoTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    TextButton(
                        onClick = onToggleMemosListMode,
                        colors = ButtonDefaults.sapTopAppBarTextButtonColors(),
                    ) {
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
            val dimens = LocalDimens.current

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = dimens.paddingMedium),
            ) {
                items(items = uiState.memos, key = { it.id }) { memo ->
                    MemoDetails(
                        memo = memo,
                        onClick = { onNavigateToMemo(memo.id) },
                        modifier = Modifier.animateItem(),
                    )
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

@Composable
private fun MemoDetails(memo: Memo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dimens = LocalDimens.current

    Surface(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(all = dimens.paddingMedium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MemoCircle {
                Text(text = memo.title.take(1))
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = dimens.spacingExtraSmall)) {
                Text(
                    text = memo.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = memo.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private inline fun MemoCircle(crossinline content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
            )
            .size(size = MEMO_CIRCLE_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiary) {
            content()
        }
    }
}

/**
 * The size of the memo circle showing the first letter of the memo title.
 */
private val MEMO_CIRCLE_SIZE = 48.dp

/**
 * The ID to pass to the Memo screen when we're intending to create a new memo.
 */
private const val DEFAULT_MEMO_ID = -1L
