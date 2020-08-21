/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.history

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class RecentlyClosedStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        RECENTLY_CLOSED,
        Context.MODE_PRIVATE
    )

    @Serializable
    data class RecentlyClosedItem(
        val title: String,
        val url: String,
        val timeClosed: Long
    )

    fun getRecentlyClosedItems(): List<RecentlyClosedItem> {
        val serializer = Json(JsonConfiguration.Stable)
        return serializer.parse(
            RecentlyClosedItem.serializer().list,
            preferences.getString(RECENTLY_CLOSED_JSON, "") ?: ""
        )
    }

    private fun saveNewEntry(list: List<RecentlyClosedItem>) {
        val serializer = Json(JsonConfiguration.Stable)
        val json = serializer.stringify(
            RecentlyClosedItem.serializer().list, list
        )
        preferences.edit()
            .putString(
                RECENTLY_CLOSED_JSON,
                json
            ).apply()
    }

    fun addRecentlyClosedItem(url: String, title: String) {
        val list = getRecentlyClosedItems()
        list.toMutableList().add(RecentlyClosedItem(title, url, System.currentTimeMillis()))
        saveNewEntry(list.toList().sortedBy { it.timeClosed }.take(5))
    }

    fun removeRecentlyClosedItem(item: RecentlyClosedItem) {
        val list = getRecentlyClosedItems()
        list.toMutableList().remove(item)
        saveNewEntry(list.toList())
    }

    fun removeAllRecentlyClosedItems() {
        preferences.edit().clear().apply()
    }

    companion object {
        const val RECENTLY_CLOSED_JSON = "recently_closed_json"
        const val RECENTLY_CLOSED = "recently_closed"
    }
}
