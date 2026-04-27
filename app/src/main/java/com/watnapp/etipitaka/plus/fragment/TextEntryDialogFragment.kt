package com.watnapp.etipitaka.plus.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

class TextEntryDialogFragment : DialogFragment() {

    enum class InputMode {
        DIGIT,
        TEXT,
    }

    interface TextEntryDialogButtonClickListener {
        fun onTextEntryDialogPositiveButtonClick(text: String, id: Int)
        fun onTextEntryDialogNegativeButtonClick()
    }

    private var inputText = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val title = args.getInt(ARG_TITLE)
        val message = args.getString(ARG_MESSAGE).orEmpty()
        val id = args.getInt(ARG_ID)
        val lines = args.getInt(ARG_LINES)
        val mode = InputMode.entries[args.getInt(ARG_MODE)]
        val note = args.getString(ARG_NOTE).orEmpty()
        inputText = note

        val inputView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ETipitakaTheme {
                    var text by remember { mutableStateOf(note) }
                    inputText = text
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = if (mode == InputMode.DIGIT) it.filter(Char::isDigit) else it
                            inputText = text
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (lines > 1) 160.dp else 64.dp)
                            .padding(top = 8.dp),
                        minLines = if (lines > 1) lines else 1,
                        maxLines = if (lines > 1) lines else 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (mode == InputMode.DIGIT) {
                                KeyboardType.Number
                            } else {
                                KeyboardType.Text
                            },
                        ),
                    )
                }
            }
        }

        return AlertDialog.Builder(requireActivity())
            .setView(inputView)
            .setTitle(if (title == 0) null else getString(title))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                notifyPositiveButtonClick(inputText, id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                notifyNegativeButtonClick()
            }
            .create()
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
            newInstance(title, message, id, 1, mode, "")

        @JvmStatic
        fun newInstance(
            title: Int,
            message: String?,
            id: Int,
            lines: Int,
        ): TextEntryDialogFragment =
            newInstance(title, message, id, 1, InputMode.DIGIT)

        @JvmStatic
        fun newInstance(title: Int, message: String?, id: Int): TextEntryDialogFragment =
            newInstance(title, message, id, 1)
    }
}
