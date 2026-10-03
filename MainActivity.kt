package com.navimeal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PrimaryBlue, onPrimary = TextWhite,
                    background = AppBg, surface = CardBg, onSurface = TextWhite,
                    onBackground = TextWhite, surfaceVariant = CardBg
                )
            ) { NaviApp() }
        }
    }
}

@Composable
fun NaviApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("cart", "Cart", Icons.Default.ShoppingCart),
        Triple("orders", "Orders", Icons.Default.Menu),
        Triple("profile", "Profile", Icons.Default.Person)
    )
    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            if (route in tabs.map { it.first }) {
                NavigationBar(containerColor = CardBg) {
                    tabs.forEach { (r, label, icon) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = {
                                nav.navigate(r) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (r == "cart" && vm.cart.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text("${vm.cart.sumOf { it.qty }}") } }) { Icon(icon, label) }
                                } else Icon(icon, label)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "splash", modifier = Modifier.padding(pad)) {
            composable("splash") {
                SplashScreen {
                    nav.navigate(if (vm.userName == null) "login" else "home") { popUpTo("splash") { inclusive = true } }
                }
            }
            composable("login") {
                AuthScreen(signup = false, vm = vm,
                    onSwitch = { nav.navigate("signup") },
                    onDone = { nav.navigate("home") { popUpTo("login") { inclusive = true } } })
            }
            composable("signup") {
                AuthScreen(signup = true, vm = vm,
                    onSwitch = { nav.popBackStack() },
                    onDone = { nav.navigate("home") { popUpTo("login") { inclusive = true } } })
            }
            composable("home") { HomeScreen { nav.navigate("restaurant/${it.id}") } }
            composable("restaurant/{id}") {
                val r = restaurants.first { r -> r.id == it.arguments?.getString("id")?.toInt() }
                RestaurantScreen(r, vm, onBack = { nav.popBackStack() }, onCart = { nav.navigate("cart") })
            }
            composable("cart") { CartScreen(vm, onCheckout = { nav.navigate("checkout") }) }
            composable("checkout") {
                CheckoutScreen(vm, onBack = { nav.popBackStack() }) { order ->
                    nav.navigate("tracking/${order.id}") { popUpTo("home") }
                }
            }
            composable("tracking/{id}") {
                val o = vm.orders.first { o -> o.id == it.arguments?.getString("id") }
                TrackingScreen(o, onBack = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onRate = { nav.navigate("orders") })
            }
            composable("orders") { OrdersScreen(vm) { nav.navigate("tracking/${it.id}") } }
            composable("profile") {
                ProfileScreen(vm) { vm.logout(); nav.navigate("login") { popUpTo(0) } }
            }
        }
    }
}
