@file:OptIn(ExperimentalMaterial3Api::class)

package com.codebxlk.compose.translator.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.codebxlk.compose.translator.extension.composable.AlertDialog
import com.codebxlk.compose.translator.extension.composable.SearchAppBar
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState
import com.codebxlk.compose.translator.data.model.LanguageState.AUTO
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADED
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADING
import com.codebxlk.compose.translator.data.model.LanguageState.NONE
import com.codebxlk.compose.translator.data.model.LanguageState.SUPPORTED
import com.codebxlk.compose.translator.data.model.SelectedType
import com.codebxlk.compose.translator.extension.toast
import com.codebxlk.compose.translator.ui.theme.TranslatorTheme
import com.codebxlk.compose.translator.viewmodel.LanguageViewModel
import com.codebxlk.compose.translator.viewmodel.ViewModelFactoryProvider
import dagger.hilt.android.EntryPointAccessors
import timber.log.Timber

@Composable
fun languageViewModel(selectedType: String): LanguageViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).languageViewModelFactory()

    return viewModel(factory = LanguageViewModel.provideFactory(factory, selectedType))
}

@Composable
fun LanguageScreen(
    navController: NavHostController,
    selectedType: String
) {
    val context = LocalContext.current

    val viewModel = languageViewModel(selectedType)
    val isFromSource = selectedType == SelectedType.SOURCE.name
    val screenTitle = "Translate ${if (isFromSource) "from" else "to"}"

    val languageList = viewModel.languageList.collectAsLazyPagingItems()
    val sourceLanguage by viewModel.run { sourceLanguage.collectAsState(defaultSource) }
    val targetLanguage by viewModel.run { targetLanguage.collectAsState(defaultTarget) }

    var index by rememberSaveable { mutableIntStateOf(0) }
    val lazyListState: LazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var openAutoDialog by rememberSaveable { mutableStateOf(false) }
    var openDownloadDialog by rememberSaveable { mutableStateOf(false) }
    var openLRemoveDialog by rememberSaveable { mutableStateOf(false) }

    var languageId by rememberSaveable { mutableStateOf("") }
    var languageName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchAppBar(
                title = screenTitle,
                navIcon = Icons.Filled.ArrowBack,
                scrollBehavior = scrollBehavior,
                onNavClick = { navController.navigateUp() },
                onQueryChanged = { viewModel.setSearchTerm(it) }
            )
        }
    ) { paddingValues ->
        LanguageItemList(
            modifier = Modifier.padding(paddingValues),
            lazyListState = lazyListState,
            languageList = languageList,
            sourceLanguageName = sourceLanguage.languageName,
            onItemClick = {
                onLanguageSelect(
                    languageId = it.languageId,
                    isFromSource = isFromSource,
                    sourceLanguageId = sourceLanguage.languageId,
                    targetLanguageId = targetLanguage.languageId,
                    onAutoWarning = { openAutoDialog = true },
                    onSwapSelected = {
                        viewModel.swapLanguage(
                            isFromSource = isFromSource,
                            sourceLanguageId = sourceLanguage.languageId,
                            targetLanguageId = targetLanguage.languageId
                        )
                    },
                    onSaveSelected = {
                        viewModel.saveLanguage(
                            isFromSource = isFromSource,
                            languageId = it.languageId
                        )
                    }
                )
            },
            onItemActionClick = {
                languageId = it.languageId
                languageName = it.languageName

                when (it.languageState) {
                    NONE, AUTO, DOWNLOADING -> {}
                    SUPPORTED -> openDownloadDialog = true
                    DOWNLOADED -> openLRemoveDialog = true
                }
            }
        )
    }

    if (openAutoDialog) {
        AlertDialog(
            icon = Icons.Rounded.Info,
            dialogTitle = "Attention Please",
            dialogText = "This language is already selected as a target language. So please select another language.",
            onPositiveClick = { openAutoDialog = false },
            onDismissRequest = { openAutoDialog = false }
        )
    }

    if (openDownloadDialog) {
        AlertDialog(
            icon = Icons.Rounded.FileDownload,
            dialogTitle = "Download $languageName",
            dialogText = "Translate this language even when you are offline by downloading an offline translation file.",
            positiveBtnText = "Download",
            negativeBtnText = "Cancel",
            onPositiveClick = {
                Timber.d("$languageName language model download started.")
                openDownloadDialog = false

                viewModel.downloadLanguage(
                    languageId = languageId,
                    isWiFiRequired = false,
                    onSuccessListener = {
                        val message =
                            "$languageName language model download successfully."
                        Timber.d(message)
                        context.toast(message)
                    },
                    onFailureListener = {
                        val message =
                            "Failed to download the $languageName language model."
                        Timber.d(message)
                        context.toast(message)
                    }
                )
            },
            onDismissRequest = { openDownloadDialog = false }
        )
    }

    if (openLRemoveDialog) {
        AlertDialog(
            icon = Icons.Rounded.DeleteOutline,
            dialogTitle = "Delete $languageName",
            dialogText = "If you remove this offline translation file, this language will be unavailable for offline translation.",
            positiveBtnText = "Delete",
            negativeBtnText = "Cancel",
            onPositiveClick = {
                Timber.d("$languageName language model removed started.")
                openLRemoveDialog = false

                viewModel.deleteLanguage(
                    languageId = languageId,
                    onSuccessListener = {
                        val message =
                            "$languageName language model removed successfully."
                        Timber.d(message)
                        context.toast(message)
                    },
                    onFailureListener = {
                        val message =
                            "Failed to remove the $languageName language model."
                        Timber.d(message)
                        context.toast(message)
                    }
                )
            },
            onDismissRequest = { openLRemoveDialog = false }
        )
    }
}

fun onLanguageSelect(
    languageId: String,
    isFromSource: Boolean,
    sourceLanguageId: String,
    targetLanguageId: String,
    onAutoWarning: () -> Unit,
    onSwapSelected: () -> Unit,
    onSaveSelected: () -> Unit,
) {
    if (isFromSource && sourceLanguageId == "auto" && targetLanguageId == languageId) {
        onAutoWarning()
        return
    }

    if (languageId == targetLanguageId) {
        onSwapSelected()
    } else {
        onSaveSelected()
    }
}

@Composable
fun LanguageItemList(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    languageList: LazyPagingItems<Language>,
    sourceLanguageName: String,
    onItemClick: (Language) -> Unit,
    onItemActionClick: (Language) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        state = lazyListState,
        horizontalAlignment = Alignment.Start,
    ) {
        items(
            count = languageList.itemCount,
            key = languageList.itemKey(),
            contentType = languageList.itemContentType()
        ) { index ->
            val item = languageList[index] ?: return@items

            LanguageItem(
                title = item.languageName,
                sourceLanguageName = sourceLanguageName,
                languageState = item.languageState,
                onItemClick = { onItemClick(item) },
                onItemActionClick = { onItemActionClick(item) }
            )
        }
    }
}

@Preview
@Composable
fun LanguageItemPreview() {
    val english = Language(
        languageId = "en",
        languageName = "English",
        time = 0L,
        languageState = DOWNLOADED
    )

    TranslatorTheme {
        Surface {
            LanguageItem(
                title = english.languageName,
                sourceLanguageName = "English",
                languageState = DOWNLOADING,
                onItemClick = {},
                onItemActionClick = {}
            )
        }
    }
}

@Composable
fun LanguageItem(
    modifier: Modifier = Modifier,
    title: String,
    sourceLanguageName: String,
    languageState: LanguageState,
    onItemClick: () -> Unit,
    onItemActionClick: () -> Unit
) {
    val isChecked = title == sourceLanguageName
    val color = MaterialTheme.colorScheme.run {
        if (isChecked) secondaryContainer else background
    }
    val iconColor = MaterialTheme.colorScheme.run {
        if (isChecked) onSecondaryContainer else primary
    }
    val textColor = MaterialTheme.colorScheme.run {
        if (isChecked) onSecondaryContainer else onBackground
    }

    ConstraintLayout(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(color, MaterialTheme.shapes.extraLarge)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onItemClick() }
            .then(modifier)
    ) {
        val (leading, text, trailing) = createRefs()

        val startGuideline = createGuidelineFromStart(48.dp)
        val endGuideline = createGuidelineFromEnd(48.dp)

        if (isChecked) {
            Icon(
                imageVector = Icons.Filled.Check,
                tint = iconColor,
                contentDescription = null,
                modifier = Modifier.constrainAs(leading) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(startGuideline)
                }
            )
        }

        Text(
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            text = title,
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(startGuideline, margin = 8.dp)
                end.linkTo(endGuideline, margin = 8.dp)
                width = Dimension.fillToConstraints
            }
        )

        ItemTrailing(
            Modifier.constrainAs(trailing) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(endGuideline)
                end.linkTo(parent.end)
            }, languageState, iconColor, onItemActionClick
        )
    }
}

@Composable
fun ItemTrailing(
    modifier: Modifier = Modifier,
    languageState: LanguageState,
    iconColor: Color,
    onClick: () -> Unit
) {
    when (languageState) {
        NONE -> {}
        AUTO -> Icon(Icons.Outlined.AutoAwesome, null, modifier, iconColor)
        DOWNLOADING -> CircularProgressIndicator(modifier, iconColor)
        SUPPORTED, DOWNLOADED -> {
            IconButton(
                modifier = modifier,
                onClick = onClick,
                content = {
                    Icon(
                        Icons.Outlined.run {
                            if (languageState == DOWNLOADED) Delete else FileDownload
                        }, null, tint = iconColor
                    )
                }
            )
        }
    }
}