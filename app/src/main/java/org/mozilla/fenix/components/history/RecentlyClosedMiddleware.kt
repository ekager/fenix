/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.history

import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareStore

/**
 * [Middleware] implementation for storing recently closed tabs.
 */
class RecentlyClosedMiddleware(
) : Middleware<BrowserState, BrowserAction> {
    override fun invoke(
        store: MiddlewareStore<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction
    ) {
        when (action) {
            is TabListAction.RemoveAllNormalTabsAction -> {
                store.state.tabs.filterNot { it.content.private }.forEach { tab ->
                    // Add to recently closed storage
                }
            }
            is TabListAction.RemoveAllPrivateTabsAction -> {
                store.state.tabs.filter { it.content.private }.forEach { tab ->
                    // Add to recently closed storage
                }
            }
            is TabListAction.RemoveAllTabsAction -> {
                store.state.tabs.forEach { tab ->
                    // Add to recently closed storage
                }
            }
            is TabListAction.RemoveTabAction -> {
                // Add to recently closed storage
            }
        }

        next(action)
    }
}
