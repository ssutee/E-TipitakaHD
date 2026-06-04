package com.watnapp.etipitaka.plus.model

import android.content.Context
import android.util.Log
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import java.util.*
import kotlin.math.roundToInt

open class ETThaiSupremeDataModel(context: Context) : ETBasicDataModel(context) {
    protected val TAG = "ETThaiSupremeDataModel"

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
        if (listener == null) return
        // Map lookup may return null (page not in the convert map) — previously
        // `pair!!` NPEd. Treat missing as "no items" instead of crashing.
        val pair = BookDatabaseHelper.getThaiMSConvertItemMap(mContext)?.get(
                String.format(Locale.getDefault(), "v%d-p%d", volume, page)) as? List<*>
        if (pair == null || pair.size < 2 || pair[0] == null || pair[1] == null) {
            listener.onGetItemsFinish(null, null)
            return
        }
        try {
            val item = pair[0].toString().toFloat().roundToInt()
            val section = pair[1].toString().toFloat().roundToInt()
            listener.onGetItemsFinish(arrayOf(item), arrayOf(section))
        } catch (e: Exception) {
            Log.e(TAG, "getComparingItemsAtPage failed for volume=$volume, page=$page", e)
            listener.onGetItemsFinish(null, null)
        }
    }

    override fun getPagesByItem(volume: Int, item: Int, needConvertToSiamrat: Boolean): Array<Int> {
        if (!needConvertToSiamrat) {
            return super.getPagesByItem(volume, item, false)
        }
        val convertMap = BookDatabaseHelper.getThaiMSConvertItemMap(mContext) ?: return emptyArray()
        // The MS mapping table has scattered gaps for some items. When the
        // exact item is absent, walk down to the nearest lower item that
        // exists so jump-to-item by Siam indexing lands on the right page
        // vicinity instead of failing silently. Mirrors PC reference
        // Engine._NearestItemPage and iOS ETDifferIndexModel.queryPages.
        var target = item
        while (target > 0) {
            val pages = ArrayList<Int>()
            var section = 1
            while (true) {
                val page = convertMap[String.format(Locale.getDefault(), "v%d-%d-i%d", volume, section, target)]
                        ?: break
                try {
                    pages.add(Math.round(page.toString().toFloat()))
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "getPagesByItem: bad page value $page for volume=$volume, item=$target, section=$section")
                }
                section += 1
            }
            if (pages.isNotEmpty()) {
                return pages.toTypedArray()
            }
            target -= 1
        }
        return emptyArray()
    }

    override fun getPageByItem(volume: Int, item: Int, section: Int, needConvertToSiamrat: Boolean): Int {
        if (!needConvertToSiamrat) {
            return super.getPageByItem(volume, item, section, false)
        }
        val convertMap = BookDatabaseHelper.getThaiMSConvertItemMap(mContext) ?: return 0
        // Mapping table has scattered gaps. Walk down to the nearest lower
        // item that exists so compare lands on the right page vicinity
        // instead of page 0. Mirrors PC reference Engine._NearestItemPage
        // and iOS ETDifferIndexModel.convert(fromPivot:).
        var i = item
        while (i > 0) {
            val page = convertMap[String.format(Locale.getDefault(), "v%d-%d-i%d", volume, section, i)]
            if (page != null) {
                try {
                    return page.toString().toFloat().roundToInt()
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "getPageByItem: bad page value $page for volume=$volume, item=$i, section=$section")
                }
            }
            i -= 1
        }
        return 0
    }


}