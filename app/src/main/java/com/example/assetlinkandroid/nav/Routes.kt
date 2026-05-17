package com.example.assetlinkandroid.nav

object Routes {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val BROWSE = "browse"
    const val MY_ITEMS = "my_items"
    const val MY_LOANS = "my_loans"
    const val NOTIFICATIONS = "notifications"
    const val ITEMS_FOR_SALE = "items_for_sale"
    const val ITEM_DETAIL = "item/{itemId}"

    fun itemDetail(itemId: String) = "item/$itemId"

    /**
     * Maps a web-app notification `link` (e.g. "/my-loans", "/items/{id}")
     * to the corresponding Android route. Returns null if unmappable.
     */
    fun fromWebLink(link: String?): String? {
        if (link.isNullOrBlank()) return null
        val path = link.trim().removePrefix("/")
        return when {
            path == "my-loans"       -> MY_LOANS
            path == "my-items"       -> MY_ITEMS
            path == "notifications"  -> NOTIFICATIONS
            path == "loan-officer"   -> DASHBOARD          // no LO page in Android → fallback
            path.startsWith("items/") -> {
                val id = path.removePrefix("items/")
                if (id.isNotBlank()) itemDetail(id) else null
            }
            else -> null
        }
    }
}
