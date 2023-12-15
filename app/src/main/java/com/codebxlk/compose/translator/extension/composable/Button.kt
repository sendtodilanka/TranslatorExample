package com.codebxlk.compose.translator.extension.composable

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun ButtonWithTextPreview() {
    ButtonWithText(
        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 56.dp),
        buttonText = "Test",
        onClick = { }
    )
}

@Composable
fun ButtonWithText(
    modifier: Modifier = Modifier,
    buttonText: String,
    shapes: RoundedCornerShape = RoundedCornerShape(10.dp),
    enabled: Boolean = true,
    elevation: ButtonElevation? = null,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        shape = shapes,
        elevation = elevation,
        onClick = onClick,
        content = { Text(text = buttonText) }
    )
}