package com.example.assetlinkandroid.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.auth.AuthScreen
import com.example.assetlinkandroid.ui.browse.BrowseScreen
import com.example.assetlinkandroid.ui.dashboard.DashboardScreen
import com.example.assetlinkandroid.ui.itemdetail.ItemDetailScreen
import com.example.assetlinkandroid.ui.itemsforsale.ItemsForSaleScreen
import com.example.assetlinkandroid.ui.myitems.MyItemsScreen
import com.example.assetlinkandroid.ui.myloans.MyLoansScreen
import com.example.assetlinkandroid.ui.notifications.NotificationsScreen
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

private data class SidebarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val sidebarItems = listOf(
    SidebarItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    SidebarItem(Routes.BROWSE, "Browse", Icons.Filled.Storefront),
    SidebarItem(Routes.MY_ITEMS, "My Items", Icons.Filled.Inventory2),
    SidebarItem(Routes.MY_LOANS, "My Loans", Icons.Filled.AccountBalance),
    SidebarItem(Routes.NOTIFICATIONS, "Inbox", Icons.Filled.Notifications),
    SidebarItem(Routes.ITEMS_FOR_SALE, "Items for Sale", Icons.Filled.ShoppingCart),
)

@Composable
fun AppNav(appVm: AppViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val sessionStatus by appVm.sessionStatus.collectAsStateWithLifecycle()

    when (sessionStatus) {
        SessionStatus.Initializing -> LoadingFullscreen()
        is SessionStatus.NotAuthenticated -> AuthScreen(onAuthenticated = { /* status flow re-routes */ })
        is SessionStatus.Authenticated -> AuthenticatedShell(nav, appVm)
        else -> LoadingFullscreen()
    }
}

@Composable
private fun AuthenticatedShell(nav: NavHostController, appVm: AppViewModel) {
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val isTopLevel = sidebarItems.any { it.route == currentRoute }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    val session by appVm.session.collectAsStateWithLifecycle()
    val unread by appVm.unreadNotifications.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevel,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                unreadCount = unread,
                userEmail = session?.email,
                userRoles = session?.roles?.map { it.name.lowercase() }.orEmpty(),
                onItemClick = { route ->
                    closeDrawer()
                    if (currentRoute != route) {
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onLogout = {
                    closeDrawer()
                    appVm.signOut()
                },
            )
        },
    ) {
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    appVm = appVm,
                    onItemClick = { id -> nav.navigate(Routes.itemDetail(id)) },
                    onMenuClick = openDrawer,
                    onNavToMyItems = {
                        nav.navigate(Routes.MY_ITEMS) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavToMyLoans = {
                        nav.navigate(Routes.MY_LOANS) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavToNotifications = {
                        nav.navigate(Routes.NOTIFICATIONS) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavToBrowse = {
                        nav.navigate(Routes.BROWSE) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.BROWSE) {
                BrowseScreen(
                    onItemClick = { id -> nav.navigate(Routes.itemDetail(id)) },
                    onMenuClick = openDrawer,
                )
            }
            composable(Routes.MY_ITEMS) {
                MyItemsScreen(
                    appVm = appVm,
                    onItemClick = { id -> nav.navigate(Routes.itemDetail(id)) },
                    onMenuClick = openDrawer,
                )
            }
            composable(Routes.MY_LOANS) {
                MyLoansScreen(appVm = appVm, onMenuClick = openDrawer)
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    appVm = appVm,
                    onMenuClick = openDrawer,
                    onNavigate = { route ->
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.ITEMS_FOR_SALE) {
                ItemsForSaleScreen(appVm = appVm, onMenuClick = openDrawer)
            }
            composable(
                route = Routes.ITEM_DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) { entry ->
                val itemId = entry.arguments?.getString("itemId") ?: return@composable
                ItemDetailScreen(
                    itemId = itemId,
                    appVm = appVm,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun AppDrawerContent(
    currentRoute: String?,
    unreadCount: Int,
    userEmail: String?,
    userRoles: List<String>,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header: logo + brand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "A",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        "AssetLink",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Pawn marketplace",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()

            Spacer(Modifier.height(8.dp))

            // Navigation items
            sidebarItems.forEach { item ->
                val selected = currentRoute == item.route
                val showBadge = item.route == Routes.NOTIFICATIONS && unreadCount > 0
                NavigationDrawerItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    badge = if (showBadge) {
                        {
                            Text(
                                if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    } else null,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // Profile section
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val initials = userEmail
                    ?.takeIf { it.isNotBlank() }
                    ?.firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        userEmail ?: "Signed in",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (userRoles.isEmpty()) "—"
                        else userRoles.joinToString(" · ") {
                            it.replaceFirstChar(Char::uppercase)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                )
            }
            HorizontalDivider()

            // Logout
            NavigationDrawerItem(
                selected = false,
                onClick = onLogout,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                label = {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LoadingFullscreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
