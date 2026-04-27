package com.watnapp.etipitaka.plus.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ETipitakaTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

@Composable
fun ComposeFoundationSmokeText(text: String) {
    ETipitakaTheme {
        Surface {
            Text(text = text)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComposeFoundationSmokeTextPreview() {
    ComposeFoundationSmokeText(text = "E-Tipitaka")
}
