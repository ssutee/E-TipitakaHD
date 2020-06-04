package com.watnapp.etipitaka.plus.model

import android.content.Context
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import java.util.*
import kotlin.math.roundToInt

open class ETThaiSupremeDataModel(context: Context) : ETBasicDataModel(context) {
    protected val TAG = "ETThaiMahaChulaDataModel"

    override fun search(keywords: String?, listener: BookDatabaseHelper.OnSearchListener?) {
        search(keywords, listener, arrayOf(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45
        ))
    }

    override fun getShortTitle(): String = mContext.getString(R.string.thaims_short_name)

    override fun getLanguage(): BookDatabaseHelper.Language = BookDatabaseHelper.Language.THAIMS

    override fun getSectionBoundary(index: Int): Int = when(index) {
        0 -> 8
        1 -> 33
        else -> 45
    }

    override fun hasFooter(): Boolean = true

    override fun getFooterColumn(): String = "footer"

    override fun getTotalVolumes(): Int = 45

    override fun getDatabasePath(): String = Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAIMS)

    override fun getBookItems(): MutableMap<String, MutableMap<String, MutableMap<String, ArrayList<Int>>>> =
            BookDatabaseHelper.getThaiMSBookItems(mContext)

    override fun getComparingItemsAtPage(volume: Int, page: Int, listener: BookDatabaseHelper.OnGetItemsListener?) {
        val pair = BookDatabaseHelper.getThaiMSConvertItemMap(mContext)[String.format("v%d-p%d", volume, page)] as List<Double>?
        listener!!.onGetItemsFinish(arrayOf(pair!![0].toString().toFloat().roundToInt()),
                arrayOf(pair[1].toString().toFloat().roundToInt()))
    }

    override fun getPagesByItem(volume: Int, item: Int, needConvertToSiamrat: Boolean): Array<Int> {
        if (needConvertToSiamrat) {
            val pages = ArrayList<Int>()
            var section = 1
            while (true) {
                val page = BookDatabaseHelper.getThaiMSConvertItemMap(mContext)[String.format("v%d-%d-i%d", volume, section, item)]
                if (page != null) {
                    pages.add(Math.round(page.toString().toFloat()))
                    section += 1
                    continue
                }
                break
            }
            return pages.toTypedArray()
        }
        return super.getPagesByItem(volume, item, false)
    }

    override fun getPageByItem(volume: Int, item: Int, section: Int, needConvertToSiamrat: Boolean): Int {
        if (!needConvertToSiamrat) {
            return super.getPageByItem(volume, item, section, false)
        }
        val page = BookDatabaseHelper.getThaiMSConvertItemMap(mContext)[String.format("v%d-%d-i%d", volume, section, item)]
        return page?.toString()?.toFloat()?.roundToInt() ?: 0
    }


}