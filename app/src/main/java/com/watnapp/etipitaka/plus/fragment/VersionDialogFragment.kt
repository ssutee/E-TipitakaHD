package com.watnapp.etipitaka.plus.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import com.watnapp.etipitaka.plus.BuildConfig
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

class VersionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ETipitakaTheme {
                VersionDialogContent(onDismiss = { dismiss() })
            }
        }
    }
}

@Composable
private fun VersionDialogContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val iconBitmap = remember(context) {
        context.packageManager
            .getApplicationIcon(context.packageName)
            .toBitmap(width = 192, height = 192)
            .asImageBitmap()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.app_name)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(
                        R.string.version_label_build,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    )
                )
                Text(
                    stringResource(
                        R.string.version_label_device,
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                    )
                )
                Text(
                    stringResource(
                        R.string.version_label_sdks,
                        BuildConfig.MIN_SDK,
                        BuildConfig.TARGET_SDK,
                        BuildConfig.COMPILE_SDK,
                    )
                )
            }
        },
    )
}
