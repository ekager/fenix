/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.history

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.mozilla.fenix.library.SelectionHolder
import org.mozilla.fenix.library.history.viewholders.HistoryListItemViewHolder
import java.util.Calendar
import java.util.Date

class RecentlyClosedAdapter(
    private val historyInteractor: HistoryInteractor
) : ListAdapter<HistoryItem, HistoryListItemViewHolder>(historyDiffCallback), SelectionHolder<HistoryItem> {

    private var mode: HistoryFragmentState.Mode = HistoryFragmentState.Mode.Normal
    override val selectedItems get() = mode.selectedItems
    var pendingDeletionIds = emptySet<Long>()
    private val itemsWithHeaders: MutableMap<HistoryItemTimeGroup, Int> = mutableMapOf()

    override fun getItemViewType(position: Int): Int = HistoryListItemViewHolder.LAYOUT_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return HistoryListItemViewHolder(view, historyInteractor, this)
    }

    override fun onBindViewHolder(holder: HistoryListItemViewHolder, position: Int) {
        val current = getItem(position) ?: return
        val headerForCurrentItem = timeGroupForHistoryItem(current)
        val isPendingDeletion = pendingDeletionIds.contains(current.visitedAt)
        var timeGroup: HistoryItemTimeGroup? = null

        // Add or remove the header and position to the map depending on it's deletion status
        if (itemsWithHeaders.containsKey(headerForCurrentItem)) {
            if (isPendingDeletion && itemsWithHeaders[headerForCurrentItem] == position) {
                itemsWithHeaders.remove(headerForCurrentItem)
            } else if (isPendingDeletion && itemsWithHeaders[headerForCurrentItem] != position) {
                // do nothing
            } else {
                if (position <= itemsWithHeaders[headerForCurrentItem] as Int) {
                    itemsWithHeaders[headerForCurrentItem] = position
                    timeGroup = headerForCurrentItem
                }
            }
        } else if (!isPendingDeletion) {
            itemsWithHeaders[headerForCurrentItem] = position
            timeGroup = headerForCurrentItem
        }

        holder.bind(current, timeGroup, position == 0, mode, isPendingDeletion)
    }

    companion object {
        private const val zeroDays = 0
        private const val sevenDays = 7
        private const val thirtyDays = 30
        private val oneDayAgo = getDaysAgo(zeroDays).time
        private val sevenDaysAgo = getDaysAgo(sevenDays).time
        private val thirtyDaysAgo = getDaysAgo(thirtyDays).time
        private val lastWeekRange = LongRange(sevenDaysAgo, oneDayAgo)
        private val lastMonthRange = LongRange(thirtyDaysAgo, sevenDaysAgo)

        private fun getDaysAgo(daysAgo: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

            return calendar.time
        }

        private fun timeGroupForHistoryItem(item: HistoryItem): HistoryItemTimeGroup {
            return when {
                DateUtils.isToday(item.visitedAt) -> HistoryItemTimeGroup.Today
                lastWeekRange.contains(item.visitedAt) -> HistoryItemTimeGroup.ThisWeek
                lastMonthRange.contains(item.visitedAt) -> HistoryItemTimeGroup.ThisMonth
                else -> HistoryItemTimeGroup.Older
            }
        }

        private val historyDiffCallback = object : DiffUtil.ItemCallback<HistoryItem>() {
            override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: HistoryItem, newItem: HistoryItem): Any? {
                return newItem
            }
        }
    }
}
