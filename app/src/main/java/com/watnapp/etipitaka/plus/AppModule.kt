package com.watnapp.etipitaka.plus

import com.watnapp.etipitaka.plus.adapter.EnglishDictAdapter
import com.watnapp.etipitaka.plus.adapter.PaliDictAdapter
import com.watnapp.etipitaka.plus.adapter.ThaiDictAdapter
import com.watnapp.etipitaka.plus.helper.EnglishDictDatabaseHelper
import com.watnapp.etipitaka.plus.helper.PaliDictDatabaseHelper
import com.watnapp.etipitaka.plus.helper.ThaiDictDatabaseHelper
import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper
import com.watnapp.etipitaka.plus.model.HistoryDaoHelper
import com.watnapp.etipitaka.plus.model.HistoryItemDaoHelper
import com.watnapp.etipitaka.plus.vm.SharedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel

import org.koin.dsl.module

val appModule = module {
    single { EnglishDictDatabaseHelper(androidContext()) }
    single { PaliDictDatabaseHelper(androidContext()) }
    single { ThaiDictDatabaseHelper(androidContext()) }

    single { EnglishDictAdapter(androidContext()) }
    single { PaliDictAdapter(androidContext()) }
    single { ThaiDictAdapter(androidContext()) }

    single { HistoryItemDaoHelper(androidContext()) }
    single { FavoriteDaoHelper(androidContext()) }
    single { HistoryDaoHelper(androidContext()) }
    viewModel { SharedViewModel() }
}