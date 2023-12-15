package com.codebxlk.compose.translator.extension.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.codebxlk.compose.translator.ui.theme.TranslatorTheme

@Preview
@Composable
fun AlertDialogWithBannerPreview() {
    TranslatorTheme {
        AlertDialogWithBanner(
            icon = Icons.Rounded.Info,
            dialogTitle = "Attention Please",
            dialogText = "This language is already selected as a target language. So please select another language.",
            onPositiveClick = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun AlertDialogWithBanner(
    icon: ImageVector,
    dialogTitle: String,
    dialogText: String,
    onPositiveClick: () -> Unit,
    onDismissRequest: () -> Unit
) {

}


@Preview
@Composable
fun AlertDialogPreview() {
    TranslatorTheme {
        AlertDialog(
            icon = Icons.Rounded.Info,
            dialogTitle = "Attention Please",
            dialogText = "This language is already selected as a target language. So please select another language.",
            onPositiveClick = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun AlertDialog(
    icon: ImageVector,
    dialogTitle: String,
    dialogText: String,
    positiveBtnText: String = "Dismiss",
    onPositiveClick: () -> Unit,
    negativeBtnText: String? = null,
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Alert Dialog Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPositiveClick()
                }
            ) {
                Text(positiveBtnText)
            }
        },
        dismissButton = {
            negativeBtnText?.let {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(it)
                }
            }
        }
    )
}