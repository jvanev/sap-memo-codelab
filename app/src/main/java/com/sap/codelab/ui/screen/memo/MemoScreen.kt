package com.sap.codelab.ui.screen.memo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun MemoScreen(viewModel: MemoViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MemoScreen(
        uiState = uiState,
        onSubmitMemo = viewModel::onSaveMemo,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoScreen(
    uiState: MemoUiState,
    onSubmitMemo: (String, String, Double, Double) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val stringRes = if (uiState.isEditing) {
                        R.string.edit_memo
                    } else {
                        R.string.new_memo
                    }

                    Text(text = stringResource(id = stringRes))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.return_to_home),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO: Submit Memo */ }) {
                        Text(text = stringResource(id = R.string.save))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Memo Screen")
        }
    }
}
