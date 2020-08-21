package org.mozilla.fenix.library.history

import android.content.Context

class RecentlyClosedStorage(context: Context) {
    private val preferences = context.getSharedPreferences(
        RECENTLY_CLOSED,
        Context.MODE_PRIVATE
    )

    fun addEntry(url: String) {
        // To do add this entry
    }

    fun removeEntry(url: String) {
        // Todo remove this entry
    }

    fun removeAll() {
        preferences.edit().clear().apply()
    }

    companion object {
        const val RECENTLY_CLOSED = "recently_closed"
    }
}
