package com.example.assetlinkandroid.nav

object Routes {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val MY_ITEMS = "my_items"
    const val MY_LOANS = "my_loans"
    const val NOTIFICATIONS = "notifications"
    const val ITEM_DETAIL = "item/{itemId}"

    fun itemDetail(itemId: String) = "item/$itemId"
}
