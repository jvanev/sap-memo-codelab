package com.sap.codelab.ui.screen.memo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.theme.LocalDimens
import com.sap.codelab.ui.theme.LocalWindowWidthSizeClass

@Composable
fun MemoScreen(viewModel: MemoViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
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
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val memoFormState = rememberMemoFormState(
        memo = uiState.memo,
        shouldMoveMapCameraToMemo = uiState.isInitialized && uiState.isEditing,
    )
    var errorMessageRes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(errorMessageRes) {
        errorMessageRes?.let { res ->
            snackbarHostState.showSnackbar(resources.getString(res))
            errorMessageRes = null
        }
    }
    Scaffold(
        modifier = Modifier.pointerInput(key1 = Unit) {
            // Clear the focus when the user taps anywhere outside a composable
            // that intercepts tap gestures. This will allow the user to tap
            // an empty space on the screen to hide the soft keyboard
            detectTapGestures { focusManager.clearFocus() }
        },
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
                    TextButton(
                        onClick = {
                            if (memoFormState.isMemoDataValid()) {
                                memoFormState.memoMarker?.position?.let { position ->
                                    onSubmitMemo(
                                        memoFormState.memoTitle,
                                        memoFormState.memoDescription,
                                        position.latitude,
                                        position.longitude,
                                    )
                                }
                            } else {
                                errorMessageRes = when {
                                    memoFormState.isMemoTitleInvalid -> R.string.memo_title_missing

                                    memoFormState.isMemoDescriptionInvalid ->
                                        R.string.memo_description_missing

                                    else -> R.string.memo_location_missing
                                }
                            }
                        },
                    ) {
                        Text(text = stringResource(id = R.string.save))
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        MemoForm(
            state = memoFormState,
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun MemoForm(state: MemoFormState, modifier: Modifier) {
    val windowSizeClass = LocalWindowWidthSizeClass.current

    if (windowSizeClass == WindowWidthSizeClass.Compact) {
        PortraitMobileScreenMemoForm(state = state, modifier = modifier)
    } else {
        LandscapeMobileScreenMemoForm(state = state, modifier = modifier)
    }
}

@Composable
private fun PortraitMobileScreenMemoForm(state: MemoFormState, modifier: Modifier = Modifier) {
    val dimens = LocalDimens.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = dimens.spacingMedium),
    ) {
        TextField(
            value = state.memoTitle,
            onValueChange = state::onMemoTitleChange,
            modifier = Modifier
                .padding(horizontal = dimens.paddingMedium)
                .fillMaxWidth(),
            label = {
                Text(text = stringResource(id = R.string.memo_title))
            },
            trailingIcon = if (state.isMemoTitleInvalid) {
                @Composable {
                    Icon(imageVector = Icons.Rounded.Error, contentDescription = null)
                }
            } else {
                null
            },
            isError = state.isMemoTitleInvalid,
            singleLine = true,
        )
        TextField(
            value = state.memoDescription,
            onValueChange = state::onMemoDescriptionChange,
            modifier = Modifier
                .padding(horizontal = dimens.paddingMedium)
                .fillMaxWidth(),
            label = {
                Text(text = stringResource(id = R.string.memo_description))
            },
            trailingIcon = if (state.isMemoDescriptionInvalid) {
                @Composable {
                    Icon(imageVector = Icons.Rounded.Error, contentDescription = null)
                }
            } else {
                null
            },
            isError = state.isMemoDescriptionInvalid,
            maxLines = MemoFormState.MEMO_DESCRIPTION_FIELD_LINES,
            minLines = MemoFormState.MEMO_DESCRIPTION_FIELD_LINES,
        )
        MemoMap(
            markerState = state.memoMarker,
            shouldMoveToMarkerPosition = state.shouldMoveMapCameraToMemo,
            onMapClick = state::onPositionChange,
            properties = state.mapProperties,
            onToggleMapType = state::toggleMapType,
            modifier = Modifier
                .weight(weight = 1f)
                .padding(horizontal = dimens.paddingMedium)
                .padding(bottom = dimens.paddingMedium),
        )
    }
}

@Composable
private fun LandscapeMobileScreenMemoForm(state: MemoFormState, modifier: Modifier = Modifier) {
    val dimens = LocalDimens.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = dimens.spacingMedium),
    ) {
        MemoMap(
            markerState = state.memoMarker,
            shouldMoveToMarkerPosition = state.shouldMoveMapCameraToMemo,
            onMapClick = state::onPositionChange,
            properties = state.mapProperties,
            onToggleMapType = state::toggleMapType,
            modifier = Modifier
                .weight(weight = 3f)
                .padding(horizontal = dimens.paddingMedium)
                .padding(bottom = dimens.paddingMedium),
        )
        Column(
            modifier = Modifier.weight(weight = 2f),
            verticalArrangement = Arrangement.spacedBy(space = dimens.spacingMedium),
        ) {
            TextField(
                value = state.memoTitle,
                onValueChange = state::onMemoTitleChange,
                modifier = Modifier
                    .padding(horizontal = dimens.paddingMedium)
                    .fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.memo_title))
                },
                trailingIcon = if (state.isMemoTitleInvalid) {
                    @Composable {
                        Icon(imageVector = Icons.Rounded.Error, contentDescription = null)
                    }
                } else {
                    null
                },
                isError = state.isMemoTitleInvalid,
                singleLine = true,
            )
            TextField(
                value = state.memoDescription,
                onValueChange = state::onMemoDescriptionChange,
                modifier = Modifier
                    .padding(horizontal = dimens.paddingMedium)
                    .padding(bottom = dimens.paddingMedium)
                    .fillMaxSize(),
                label = {
                    Text(text = stringResource(id = R.string.memo_description))
                },
                trailingIcon = if (state.isMemoDescriptionInvalid) {
                    @Composable {
                        Icon(imageVector = Icons.Rounded.Error, contentDescription = null)
                    }
                } else {
                    null
                },
                isError = state.isMemoDescriptionInvalid,
            )
        }
    }
}

@Composable
private fun MemoMap(
    markerState: MarkerState?,
    shouldMoveToMarkerPosition: Boolean,
    onMapClick: (LatLng) -> Unit,
    properties: MapProperties,
    onToggleMapType: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.then(
            other = if (markerState == null) {
                Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.extraSmall,
                    )
            } else {
                Modifier
            },
        ),
    ) {
        val cameraPositionState = rememberCameraPositionState()
        // Track the first movement of the camera. The camera should be moved only
        // on the very first non-default UI state, and only if we are editing an existing memo,
        // due to the additional zooming that needs to be performed
        val cameraMoved = rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(markerState, shouldMoveToMarkerPosition) {
            if (markerState != null) {
                if (shouldMoveToMarkerPosition && !cameraMoved.value) {
                    cameraPositionState
                        .move(
                            CameraUpdateFactory
                                .newLatLngZoom(markerState.position, INITIAL_MAP_ZOOM),
                        )
                    cameraMoved.value = true
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(markerState.position))
                }
            }
        }
        GoogleMap(
            modifier = Modifier.clip(MaterialTheme.shapes.extraSmall),
            cameraPositionState = cameraPositionState,
            properties = properties,
            onMapClick = onMapClick,
        ) {
            markerState?.let { marker -> Marker(marker) }
        }
        MapTypeButton(
            onClick = onToggleMapType,
            modifier = Modifier.padding(start = 11.dp, top = 11.dp),
        ) {
            val isNormalMap = properties.mapType == MapType.NORMAL

            Icon(
                painter = painterResource(
                    id = if (isNormalMap) {
                        R.drawable.ic_earth_americas
                    } else {
                        R.drawable.ic_map
                    },
                ),
                contentDescription = stringResource(
                    id = if (isNormalMap) {
                        R.string.google_map_satellite
                    } else {
                        R.string.google_map_normal
                    },
                ),
                modifier = Modifier.size(size = 24.dp),
            )
        }
    }
}

/**
 * The initial zoom at the current memo position, right after the state is initialized.
 */
private const val INITIAL_MAP_ZOOM = 15f

/**
 * A button with a similar style to the buttons of GoogleMaps.
 */
@Composable
private fun MapTypeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size = MapTypeButtonSize)
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(
                color = MapTypeButtonBackgroundColor,
                shape = MaterialTheme.shapes.extraSmall,
            )
            .border(
                width = 1.dp,
                color = MapTypeButtonBorderColor,
                shape = MaterialTheme.shapes.extraSmall,
            )
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MapTypeButtonIconColor) {
            content()
        }
    }
}

private val MapTypeButtonSize = 39.dp
private val MapTypeButtonIconColor = Color(color = 0xFF666666)
private val MapTypeButtonBackgroundColor = Color.White.copy(alpha = 0.8f)
private val MapTypeButtonBorderColor = Color.Gray.copy(alpha = 0.2f)

@Composable
private fun rememberMemoFormState(memo: Memo, shouldMoveMapCameraToMemo: Boolean): MemoFormState {
    return rememberSaveable(memo, shouldMoveMapCameraToMemo, saver = MemoFormState.Saver) {
        MemoFormState(memo, shouldMoveMapCameraToMemo)
    }
}

@Stable
private class MemoFormState(
    memoTitle: String,
    memoDescription: String,
    memoPosition: LatLng?,

    /**
     * Whether the camera should move to and zoom at the marker's position.
     */
    val shouldMoveMapCameraToMemo: Boolean,
) {
    /**
     * Holds the current value of the memo title field.
     */
    var memoTitle by mutableStateOf(memoTitle)
        private set

    /**
     * Whether the memo title field contains an invalid value.
     */
    var isMemoTitleInvalid by mutableStateOf(memoTitle.isEmpty())
        private set

    /**
     * Holds the current value of the memo description field.
     */
    var memoDescription by mutableStateOf(memoDescription)
        private set

    /**
     * Whether the memo description field contains an invalid value.
     */
    var isMemoDescriptionInvalid by mutableStateOf(memoDescription.isEmpty())
        private set

    /**
     * Holds the current state of the marker in the GoogleMaps composable.
     */
    var memoMarker by mutableStateOf<MarkerState?>(memoPosition?.let { MarkerState(it) })
        private set

    var mapProperties by mutableStateOf(
        MapProperties(
            isBuildingEnabled = true,
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL,
        ),
    )
        private set

    /**
     * Updates the current value of the memo title field.
     */
    fun onMemoTitleChange(title: String) {
        memoTitle = title
        isMemoTitleInvalid = title.isEmpty()
    }

    /**
     * Updates the current value of the memo description field.
     */
    fun onMemoDescriptionChange(description: String) {
        memoDescription = description
        isMemoDescriptionInvalid = description.isEmpty()
    }

    /**
     * Toggles between normal and hybrid map type.
     */
    fun toggleMapType() {
        val mapType = if (mapProperties.mapType == MapType.NORMAL) {
            MapType.HYBRID
        } else {
            MapType.NORMAL
        }
        mapProperties = mapProperties.copy(mapType = mapType)
    }

    /**
     * Updates the memo marker on the map.
     */
    fun onPositionChange(position: LatLng) {
        memoMarker = MarkerState(position)
    }

    /**
     * @return `true` if the form contains valid memo data, `false` otherwise.
     */
    fun isMemoDataValid() = !isMemoTitleInvalid && !isMemoDescriptionInvalid && memoMarker != null

    /**
     * Creates a new MemoFormState using the data of the specified [memo].
     */
    constructor(memo: Memo, shouldMoveToMarkerPosition: Boolean) : this(
        memoTitle = memo.title,
        memoDescription = memo.description,
        memoPosition = if (memo.isValidReminderPosition) {
            LatLng(memo.reminderLatitude, memo.reminderLongitude)
        } else {
            null
        },
        shouldMoveMapCameraToMemo = shouldMoveToMarkerPosition,
    )

    companion object {
        /**
         * The number of lines of the description text field.
         */
        const val MEMO_DESCRIPTION_FIELD_LINES = 3

        /**
         * The saver of `MemoFormState` instances.
         */
        val Saver: Saver<MemoFormState, *> = listSaver(
            save = {
                listOf(it.memoTitle, it.memoDescription, it.memoMarker?.position)
            },
            restore = {
                MemoFormState(
                    memoTitle = it[0] as String,
                    memoDescription = it[1] as String,
                    memoPosition = it[2] as? LatLng,
                    shouldMoveMapCameraToMemo = false,
                )
            },
        )
    }
}
