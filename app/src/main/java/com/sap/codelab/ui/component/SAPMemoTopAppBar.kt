package com.sap.codelab.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sap.codelab.ui.theme.LightGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAPMemoTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) = TopAppBar(
    title = title,
    modifier = Modifier.shadow(elevation = 5.dp),
    navigationIcon = navigationIcon,
    actions = actions,
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = LightGreen,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    ),
)
