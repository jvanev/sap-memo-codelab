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
import androidx.compose.foundation.layout.visible
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.component.SAPMemoTopAppBar
import com.sap.codelab.ui.screen.memo.MemoLaunchMode
import com.sap.codelab.ui.theme.DarkGreen
import com.sap.codelab.ui.theme.DarkRed
import com.sap.codelab.ui.theme.LocalDimens
import com.sap.codelab.ui.theme.sapTopAppBarTextButtonColors
import kotlinx.coroutines.launch

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
        onCloseMemo = viewModel::closeMemo,
        onRemoveMemo = viewModel::removeMemo,
        onUndoAction = viewModel::undoAction,
        onClearAction = viewModel::clearAction,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleMemosListMode: () -> Unit,
    onNavigateToMemo: (Long) -> Unit,
    onCloseMemo: (Memo) -> Unit,
    onRemoveMemo: (Memo) -> Unit,
    onUndoAction: () -> Unit,
    onClearAction: () -> Unit,
) {
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.undoableAction) {
        uiState.undoableAction?.let { action ->
            when (action) {
                is UndoableMemoAction.Close -> showUndoableActionSnackbar(
                    hostState = snackbarHostState,
                    action = action,
                    actionPerformedMessage = resources.getString(R.string.memo_closed),
                    actionUndoneMessage = resources.getString(R.string.mamo_reopen),
                    undoActionLabel = resources.getString(R.string.undo_action),
                    onUndoAction = onUndoAction,
                    onClearAction = onClearAction,
                )

                is UndoableMemoAction.Remove -> showUndoableActionSnackbar(
                    hostState = snackbarHostState,
                    action = action,
                    actionPerformedMessage = resources.getString(R.string.memo_removed),
                    actionUndoneMessage = resources.getString(R.string.memo_restored),
                    undoActionLabel = resources.getString(R.string.undo_action),
                    onUndoAction = onUndoAction,
                    onClearAction = onClearAction,
                )
            }
        }
    }
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToMemo(DEFAULT_MEMO_ID) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(id = R.string.create_memo),
                )
            }
        },
    ) { innerPadding ->
        val dimens = LocalDimens.current

        Box(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = dimens.paddingMedium),
            ) {
                items(items = uiState.memos, key = { it.id }) { memo ->
                    MemoContainer(
                        memo = memo,
                        onClick = { onNavigateToMemo(memo.id) },
                        onClose = { onCloseMemo(memo) },
                        onRemove = { onRemoveMemo(memo) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            if (uiState.memos.isEmpty()) {
                Text(text = stringResource(id = R.string.memo_list_empty))
            }
        }
    }
}

/**
 * Shows an [UndoableMemoAction]-related snackbar.
 * The content of the message depends on the current state of the action.
 *
 * Encapsulates the flow of actions that should happen in response to, or due to the lack of,
 * user action to this snackbar.
 *
 * The exact steps are as follows:
 * 1. The action arrives
 * 2. [actionPerformedMessage] is displayed, along with an "undo" button next to it;
 *    the button label is specified as [undoActionLabel]
 * 3. If the "undo" button is tapped, [onUndoAction] will be triggered, and, when the action
 *    state changes, [actionUndoneMessage] will be displayed, confirming that the previous
 *    state of the affected memo will be restored, and [onClearAction] will be called
 * 4. If the "undo" is not tapped, the [onClearAction] will be triggered, which will cause
 *    the changes performed by the undoable action to become permanent
 * 5.
 *
 * @param hostState The host state to be used for displaying the snackbar
 * @param action The undoable action that triggered the snackbar
 * @param actionPerformedMessage The message that describes what action has been executed
 * @param actionUndoneMessage The message that describes what happened after the user pressed
 *                            the "undo" button on the snackbar
 * @param undoActionLabel The actual label of the "undo" button appearing to the right of the
 *                        snackbar message
 * @param onUndoAction The callback to be executed if the user taps on the "undo" button
 * @param onClearAction The callback to be executed if the user don't take action in response to
 *                      the undoable action that triggered the snackbar
 */
private suspend fun showUndoableActionSnackbar(
    hostState: SnackbarHostState,
    action: UndoableMemoAction,
    actionPerformedMessage: String,
    actionUndoneMessage: String,
    undoActionLabel: String,
    onUndoAction: () -> Unit,
    onClearAction: () -> Unit,
) {
    if (action.undone) {
        hostState.showSnackbar(message = actionUndoneMessage)
        onClearAction()
    } else {
        val actionResult = hostState.showSnackbar(
            message = actionPerformedMessage,
            actionLabel = undoActionLabel,
            duration = SnackbarDuration.Short,
        )

        if (actionResult == SnackbarResult.ActionPerformed) {
            onUndoAction()
        } else {
            onClearAction()
        }
    }
}

@Composable
private fun MemoContainer(
    memo: Memo,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeState = rememberSwipeToDismissBoxState()
    var surfaceVisible by remember { mutableStateOf(true) }

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val color = if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                DarkGreen
            } else {
                DarkRed
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color),
            )
        },
        modifier = modifier.visible(visible = surfaceVisible),
        onDismiss = { result ->
            coroutineScope.launch {
                swipeState.snapTo(SwipeToDismissBoxValue.Settled)

                if (result != SwipeToDismissBoxValue.Settled) {
                    when (result) {
                        SwipeToDismissBoxValue.StartToEnd -> onClose()
                        SwipeToDismissBoxValue.EndToStart -> onRemove()
                    }
                    surfaceVisible = false
                }
            }
        },
        content = {
            MemoBody(memo = memo, onClick = onClick)
        },
    )
}

@Composable
private fun MemoBody(memo: Memo, onClick: () -> Unit) {
    val dimens = LocalDimens.current

    Surface(onClick = onClick) {
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
            Column(
                verticalArrangement = Arrangement
                    .spacedBy(space = dimens.spacingExtraSmall),
            ) {
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
