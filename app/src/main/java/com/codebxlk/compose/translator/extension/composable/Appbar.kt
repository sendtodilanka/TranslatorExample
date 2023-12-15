@file:OptIn(ExperimentalMaterial3Api::class)

package com.codebxlk.compose.translator.extension.composable

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codebxlk.compose.translator.ui.theme.TranslatorTheme

@Preview
@Composable
fun SearchAppBarPreview() {
    TranslatorTheme {
        Surface {
            SearchAppBar(
                title = "Title",
                navIcon = Icons.Filled.ArrowBack,
                onNavClick = {},
                onQueryChanged = {}
            )
        }
    }
}

@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    title: String,
    navIcon: ImageVector,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavClick: () -> Unit,
    onQueryChanged: (String) -> Unit
) {
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable { mutableStateOf("") }

    if (!showSearch) {
        value = ""
        onQueryChanged(value)
    }

    TopAppBar(
        title = {
            if (showSearch) {
                AppBarTextField(
                    value = value,
                    onValueChange = { newValue ->
                        value = newValue
                        onQueryChanged(newValue)
                    },
                    hint = "Search for languages"
                )
            } else {
                Text(text = title)
            }
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onNavClick,
                content = {
                    Icon(
                        imageVector = navIcon,
                        contentDescription = "Navigation button"
                    )
                }
            )
        },
        actions = {
            IconButton(
                onClick = { showSearch = !showSearch },
                content = {
                    Icon(
                        imageVector = Icons.Filled.run { if (showSearch) Clear else Search },
                        contentDescription = "Action button"
                    )
                }
            )
        }
    )
}

@Composable
fun AppBarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textStyle = LocalTextStyle.current
    // make sure there is no background color in the decoration box
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Unspecified,
        unfocusedContainerColor = Color.Unspecified,
        disabledContainerColor = Color.Unspecified,
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
    )

    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        MaterialTheme.colorScheme.onSurface
    }
    val mergedTextStyle = textStyle.merge(
        TextStyle(color = textColor, lineHeight = 50.sp)
    )

    // request focus when this composable is first initialized
    val focusRequester = FocusRequester()
    SideEffect { focusRequester.requestFocus() }

    // set the correct cursor position when this composable is first initialized
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                value,
                TextRange(value.length)
            )
        )
    }
    textFieldValue = textFieldValue.copy(text = value) // make sure to keep the value updated

    CompositionLocalProvider(
        LocalTextSelectionColors provides LocalTextSelectionColors.current
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                // remove newlines to avoid strange layout issues, and also because singleLine=true
                onValueChange(it.text.replace("\n", ""))
            },
            modifier = modifier
                .fillMaxWidth()
                .heightIn(32.dp)
                .focusRequester(focusRequester)
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors
                ),
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = true,
            decorationBox = { innerTextField ->
                // places text field with placeholder and appropriate bottom padding
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = false,
                    placeholder = { Text(text = hint) },
                    colors = colors,
                    contentPadding = PaddingValues(bottom = 4.dp),
                )
            }
        )
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SmallTopAppbarPreview() {
    TranslatorTheme {
        SmallTopAppbar(
            title = "Home",
            navIcon = Icons.Filled.ArrowBack,
            actionIcon = Icons.Filled.MoreVert,
            onNavClick = { },
            onActionClick = { }
        )
    }
}

@Composable
fun SmallTopAppbar(
    modifier: Modifier = Modifier,
    title: String,
    navIcon: ImageVector,
    actionIcon: ImageVector? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavClick: () -> Unit,
    onActionClick: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onNavClick,
                content = {
                    Icon(
                        imageVector = navIcon,
                        contentDescription = "Navigation button"
                    )
                }
            )
        },
        actions = {
            actionIcon?.let {
                IconButton(
                    onClick = onActionClick,
                    content = {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "Action button"
                        )
                    }
                )
            }
        }
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CenteredTopAppbarPreview() {
    TranslatorTheme {

        val appTitle = buildAnnotatedString {
            append("Offline ")
            withStyle(style = SpanStyle(MaterialTheme.colorScheme.surfaceTint)) {
                append("Translator")
            }
        }

        CenteredTopAppbar(
            title = appTitle,
            navIcon = Icons.Filled.Menu,
            actionIcon = Icons.Filled.Settings,
            onNavClick = { },
            onActionClick = { }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CenteredTopAppbar(
    modifier: Modifier = Modifier,
    title: AnnotatedString,
    navIcon: ImageVector,
    actionIcon: ImageVector,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavClick: () -> Unit,
    onActionClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onNavClick,
                content = {
                    Icon(
                        imageVector = navIcon,
                        contentDescription = "Navigation button"
                    )
                }
            )
        },
        actions = {
            IconButton(
                onClick = onActionClick,
                content = {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action button"
                    )
                }
            )
        }
    )
}