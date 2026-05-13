package com.watnapp.etipitaka.plus.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

/**
 * Text/number entry dialog. Implemented as a [DialogFragment] hosting a [ComposeView]
 * directly (via [onCreateView]) rather than wrapping a `ComposeView` in a platform
 * `AlertDialog`: a Compose `TextField` inside an `AlertDialog.Builder().setView(...)`
 * window does not raise the soft keyboard, while a plain dialog window handles the IME
 * for Compose correctly.
 */
class TextEntryDialogFragment : DialogFragment() {

    enum class InputMode {
        DIGIT,
        TEXT,
    }

    interface TextEntryDialogButtonClickListener {
        fun onTextEntryDialogPositiveButtonClick(text: String, id: Int)
        fun onTextEntryDialogNegativeButtonClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        val titleRes = args.getInt(ARG_TITLE)
        val message = args.getString(ARG_MESSAGE).orEmpty()
        val id = args.getInt(ARG_ID)
        val lines = args.getInt(ARG_LINES)
        val mode = InputMode.entries[args.getInt(ARG_MODE)]
        val note = args.getString(ARG_NOTE).orEmpty()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ETipitakaTheme {
                    TextEntryDialogContent(
                        title = if (titleRes == 0) null else stringResource(titleRes),
                        message = message,
                        initialText = note,
                        lines = lines,
                        mode = mode,
                        onConfirm = { text ->
                            notifyPositiveButtonClick(text, id)
                            dismiss()
                        },
                        onCancel = {
                            notifyNegativeButtonClick()
                            dismiss()
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.92f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun notifyPositiveButtonClick(text: String, id: Int) {
        (activity as? TextEntryDialogButtonClickListener)
            ?.onTextEntryDialogPositiveButtonClick(text, id)
        (parentFragment as? TextEntryDialogButtonClickListener)
            ?.onTextEntryDialogPositiveButtonClick(text, id)
    }

    private fun notifyNegativeButtonClick() {
        (activity as? TextEntryDialogButtonClickListener)
            ?.onTextEntryDialogNegativeButtonClick()
        (parentFragment as? TextEntryDialogButtonClickListener)
            ?.onTextEntryDialogNegativeButtonClick()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_ID = "id"
        private const val ARG_LINES = "lines"
        private const val ARG_MODE = "mode"
        private const val ARG_NOTE = "note"

        @JvmStatic
        fun newInstance(
            title: Int,
            message: String?,
            id: Int,
            lines: Int,
            mode: InputMode,
            note: String?,
        ): TextEntryDialogFragment =
            TextEntryDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putInt(ARG_ID, id)
                    putInt(ARG_LINES, lines)
                    putInt(ARG_MODE, mode.ordinal)
                    putString(ARG_NOTE, note)
                }
            }

        @JvmStatic
        fun newInstance(
            title: Int,
            message: String?,
            id: Int,
            lines: Int,
            mode: InputMode,
        ): TextEntryDialogFragment =
            newInstance(title, message, id, lines, mode, "")

        @JvmStatic
        fun newInstance(
            title: Int,
            message: String?,
            id: Int,
            lines: Int,
        ): TextEntryDialogFragment =
            newInstance(title, message, id, lines, InputMode.DIGIT)

        @JvmStatic
        fun newInstance(title: Int, message: String?, id: Int): TextEntryDialogFragment =
            newInstance(title, message, id, 1)
    }
}

@Composable
private fun TextEntryDialogContent(
    title: String?,
    message: String,
    initialText: String,
    lines: Int,
    mode: TextEntryDialogFragment.InputMode,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (!title.isNullOrEmpty()) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
            }
            if (message.isNotEmpty()) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = if (mode == TextEntryDialogFragment.InputMode.DIGIT) {
                        it.filter(Char::isDigit)
                    } else {
                        it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = lines <= 1,
                minLines = if (lines > 1) lines else 1,
                maxLines = if (lines > 1) lines else 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (mode == TextEntryDialogFragment.InputMode.DIGIT) {
                        KeyboardType.Number
                    } else {
                        KeyboardType.Text
                    },
                ),
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(android.R.string.cancel))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onConfirm(text) }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
