package com.appham.sharemarks.model

/**
 * @author thomas
 */
enum class Analytics {

    ACTION, OPEN_NAV_DRAWER, CLICK_NAV_ITEM, DELETE_MARK, DOMAIN, REFERRER, DELETED, RESTORE_MARK,
    SHARE_MARK, PRESS_BACK, FILTER, SWIPE_RIGHT, SWIPE_LEFT, CLICK_MARK, OPEN_BROWSER,
    LONG_CLICK_MARK, UNDO, CLICK_UNDO, TO_DELETE, INTENT_TYPE, INTENT_ACTION, HANDLE_INTENT, ITEM
    ;

    fun get(): String {
        return this.name.toLowerCase()
    }
}