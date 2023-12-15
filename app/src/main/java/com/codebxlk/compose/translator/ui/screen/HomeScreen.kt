@file:OptIn(ExperimentalMaterial3Api::class)

package com.codebxlk.compose.translator.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.codebxlk.compose.translator.extension.composable.CenteredTopAppbar
import com.codebxlk.compose.translator.data.model.SelectedType
import com.codebxlk.compose.translator.navigation.Screen
import com.codebxlk.compose.translator.ui.theme.TranslatorTheme
import com.codebxlk.compose.translator.viewmodel.HomeViewModel

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    TranslatorTheme {
        HomeScreenView(
            scrollBehavior = scrollBehavior,
            onNavClick = {},
            onActionClick = {},
            onSourceClick = {},
            onTargetClick = {}
        )
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    HomeScreenView(
        scrollBehavior = scrollBehavior,
        onNavClick = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route)
        },
        onActionClick = {},
        onSourceClick = {
            val selectedType = SelectedType.SOURCE.name
            navController.navigate("${Screen.Language.route}/${selectedType}")
        },
        onTargetClick = {
            val selectedType = SelectedType.TARGET.name
            navController.navigate("${Screen.Language.route}/${selectedType}")
        }
    )
}

@Composable
fun HomeScreenView(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavClick: () -> Unit,
    onActionClick: () -> Unit,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenteredTopAppbar(
                title = setupTitle(),
                navIcon = Icons.Filled.Menu,
                actionIcon = Icons.Outlined.Settings,
                scrollBehavior = scrollBehavior,
                onNavClick = onNavClick,
                onActionClick = onActionClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onSourceClick) {
                Text(text = "Source")
            }

            Button(onClick = onTargetClick) {
                Text(text = "Target")
            }
        }
    }
}

@Composable
private fun setupTitle(): AnnotatedString {
    return buildAnnotatedString {
        append("Offline ")
        withStyle(style = SpanStyle(MaterialTheme.colorScheme.surfaceTint)) {
            append("Translator")
        }
    }
}