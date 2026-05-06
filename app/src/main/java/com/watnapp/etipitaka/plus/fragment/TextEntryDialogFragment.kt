package com.watnapp.etipitaka.plus.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class TextEntryDialogFragment : DialogFragment() {

    enum class InputMode {
        DIGIT,
        TEXT,
    }

    interface TextEntryDialogButtonClickListener {
        fun onTextEntryDialogPositiveButtonClick(text: String, id: Int)
        fun onTextEntryDialogNegativeButtonClick()
    }

    private var inputEditText: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val title = args.getInt(ARG_TITLE)
        val message = args.getString(ARG_MESSAGE).orEmpty()
        val id = args.getInt(ARG_ID)
        val lines = args.getInt(ARG_LINES)
        val mode = InputMode.entries[args.getInt(ARG_MODE)]
        val note = args.getString(ARG_NOTE).orEmpty()

        val inputView = EditText(requireContext()).apply {
            setText(note)
            setSelectAllOnFocus(false)
            setSelection(text.length)
            minLines = if (lines > 1) lines else 1
            maxLines = if (lines > 1) lines else 1
            gravity = if (lines > 1) Gravity.TOP or Gravity.START else Gravity.CENTER_VERTICAL
            isSingleLine = lines <= 1
            inputType = if (mode == InputMode.DIGIT) {
                filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                    source.filter(Char::isDigit)
                })
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_TEXT or if (lines > 1) {
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
                } else {
                    0
                }
            }
        }
        inputEditText = inputView

        return AlertDialog.Builder(requireActivity())
            .setView(inputView)
            .setTitle(if (title == 0) null else getString(title))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                notifyPositiveButtonClick(inputView.text.toString(), id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                notifyNegativeButtonClick()
            }
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        inputEditText?.post {
            val editText = inputEditText ?: return@post
            editText.requestFocus()
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        inputEditText = null
        super.onDestroyView()
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
