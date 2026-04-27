package com.watnapp.etipitaka.plus.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper

class SharedViewModel : ViewModel() {
    val selected = MutableLiveData<BookDatabaseHelper.Language>()
    var resetPage = false
    fun select(language: BookDatabaseHelper.Language) {
        selected.value = language
    }
}