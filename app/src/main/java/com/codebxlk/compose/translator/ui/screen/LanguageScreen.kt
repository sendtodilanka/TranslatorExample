@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val isFromSource = selectedType == SelectedType.SOURCE.name
    val viewModel: LanguageViewModel = languageViewModel(selectedType)
    val screenTitle = "Translate ${if (isFromSource) "from" else "to"}"

    val sourceLanguage by viewModel.run { sourceLanguage.collectAsState(defaultSource) }
    //Timber.d("sourceLanguage: ${sourceLanguage.languageName}")

    val targetLanguage by viewModel.run { targetLanguage.collectAsState(defaultTarget) }
    //Timber.d("targetLanguage: ${targetLanguage.languageName}")

    val languageList = viewModel.languageList.collectAsLazyPagingItems()
    //Timber.d("languageList count: ${languageList.itemCount}")

    var index by rememberSaveable { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        lazyListState.scrollToItem(index)
    }

    var openAutoDialog by rememberSaveable { mutableStateOf(false) }
    var openLDDialog by rememberSaveable { mutableStateOf(false) }
    var openLRDialog by rememberSaveable { mutableStateOf(false) }

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
                index = lazyListState.firstVisibleItemIndex
                Timber.e("index: $index")

                languageId = it.languageId
                languageName = it.languageName

                when (it.languageState) {
                    NONE, AUTO, DOWNLOADING -> {}
                    SUPPORTED -> openLDDialog = true
                    DOWNLOADED -> openLRDialog = true
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

    if (openLDDialog) {
        AlertDialog(
            icon = Icons.Rounded.FileDownload,
            dialogTitle = "Download $languageName",
            dialogText = "Translate this language even when you are offline by downloading an offline translation file.",
            positiveBtnText = "Download",
            negativeBtnText = "Cancel",
            onPositiveClick = {
                Timber.d("$languageName language model download started.")
                openLDDialog = false

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
            onDismissRequest = { openLDDialog = false }
        )
    }

    if (openLRDialog) {
        AlertDialog(
            icon = Icons.Rounded.DeleteOutline,
            dialogTitle = "Delete $languageName",
            dialogText = "If you remove this offline translation file, this language will be unavailable for offline translation.",
            positiveBtnText = "Delete",
            negativeBtnText = "Cancel",
            onPositiveClick = {
                Timber.d("$languageName language model removed started.")
                openLRDialog = false

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
            onDismissRequest = { openLRDialog = false }
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
    languageList: LazyPagingItems<ItemLanguageAdapter>,
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
            when (val item = languageList[index]) {
                is ItemLanguageAdapter.Divider -> Divider(Modifier.padding(bottom = 16.dp))

                is ItemLanguageAdapter.Item -> LanguageItem(
                    title = item.language.languageName,
                    sourceLanguageName = sourceLanguageName,
                    languageState = item.language.languageState,
                    onItemClick = { onItemClick(item.language) },
                    onItemActionClick = { onItemActionClick(item.language) }
                )

                is ItemLanguageAdapter.SectionTitle -> ProvideTextStyle(
                    MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                ) { Text(item.title, Modifier.padding(56.dp, 24.dp)) }

                null -> throw IllegalStateException("Encountered a null item at index $index.")
            }
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