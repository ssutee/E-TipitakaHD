package com.watnapp.etipitaka.plus

import android.app.Application
import android.content.Context
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.model.History
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/5/2013
 * Time: 14:39
 *
 */
open class ETipitakaApplication : Application() {
    var language = BookDatabaseHelper.Language.THAI
    var history: History? = null

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ETipitakaApplication)
            modules(appModule)
        }
    }
}