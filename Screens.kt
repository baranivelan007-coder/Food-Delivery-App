package com.navimeal

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ---- Design tokens (from the plan's Figma palette; replace with exact Figma values) ----
val PrimaryBlue = Color(0xFF0EA5E9)
val AppBg = Color(0xFF050B16)
val CardBg = Color(0xFF1A1F28)
val TextGray = Color(0xFFB4B4B4)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val Amber = Color(0xFFFBBF24)

val customerPos = Offset(0.5f, 0.88f)

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onDone() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍽️", fontSize = 64.sp)
            Text("NAVI MEAL", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Text("Map-first food delivery", color = TextGray)
        }
    }
}

@Composable
fun AuthScreen(signup: Boolean, vm: AppViewModel, onSwitch: () -> Unit, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (signup) "Create account" else "Welcome back", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Order food from restaurants near you", color = TextGray)
        Spacer(Modifier.height(24.dp))
        if (signup) {
            OutlinedTextField(name, { name = it }, label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(if (signup) "Sign up" else "Log in") {
            error = when {
                signup && name.isBlank() -> "Enter your name"
                !email.contains("@") -> "Enter a valid email"
                pass.length < 6 -> "Password must be at least 6 characters"
                else -> null
            }
            if (error == null) {
                vm.login(if (signup) name else email.substringBefore("@"), email)
                onDone()
            }
        }
        TextButton(onClick = onSwitch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (signup) "Already have an account? Log in" else "New here? Sign up")
        }
    }
}

/** Canvas "map" – works offline with no API key. Positions are 0..1 fractions of the box. */
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    list: List<Restaurant>,
    courier: Offset? = null,
    routeFrom: Restaurant? = null,
    onTap: (Restaurant) -> Unit = {}
) {
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF0B1B2B))
            .pointerInput(list) {
                detectTapGestures { p ->
                    val w = size.width.toFloat(); val h = size.height.toFloat()
                    list.minByOrNull { (Offset(it.x * w, it.y * h) - p).getDistance() }?.let {
                        if ((Offset(it.x * w, it.y * h) - p).getDistance() < 70f) onTap(it)
                    }
                }
            }
    ) {
        val road = Color(0xFF16304A)
        for (i in 1..5) {
            drawLine(road, Offset(size.width * i / 6, 0f), Offset(size.width * i / 6, size.height), 6f)
            drawLine(road, Offset(0f, size.height * i / 6), Offset(size.width, size.height * i / 6), 6f)
        }
        fun px(o: Offset) = Offset(o.x * size.width, o.y * size.height)
        routeFrom?.let {
            drawLine(PrimaryBlue, px(Offset(it.x, it.y)), px(customerPos), 5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
        }
        list.forEach {
            val c = px(Offset(it.x, it.y))
            drawCircle(Color(0x330EA5E9), 34f, c)
            drawCircle(PrimaryBlue, 16f, c)
            drawText(measurer, it.name, Offset(c.x - 40f, c.y + 22f), TextStyle(color = Color.White, fontSize = 10.sp))
        }
        val me = px(customerPos)
        drawCircle(Color(0x3310B981), 30f, me); drawCircle(SuccessGreen, 14f, me)
        drawText(measurer, "You", Offset(me.x - 14f, me.y + 20f), TextStyle(color = Color.White, fontSize = 10.sp))
        courier?.let {
            val c = px(it)
            drawCircle(Color(0x44FBBF24), 30f, c); drawCircle(Amber, 14f, c)
            drawText(measurer, "🛵", Offset(c.x - 12f, c.y - 36f), TextStyle(fontSize = 16.sp))
        }
    }
}

@Composable
fun HomeScreen(onOpen: (Restaurant) -> Unit) {
    var query by remember { mutableStateOf("") }
    val shown = restaurants.filter { it.name.contains(query, true) || it.cuisine.contains(query, true) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Discover nearby", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Tap a pin on the map to open a restaurant", color = TextGray)
        }
        item {
            OutlinedTextField(query, { query = it }, placeholder = { Text("Search restaurants or cuisine") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item { MapView(Modifier.fillMaxWidth().height(260.dp), shown, onTap = onOpen) }
        items(shown) { r -> RestaurantCard(r) { onOpen(r) } }
        if (shown.isEmpty()) item { Text("No restaurants found", color = TextGray) }
    }
}

@Composable
fun RestaurantCard(r: Restaurant, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) { Text(r.name.first().toString(), fontSize = 24.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(r.name, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text("${r.cuisine} • ${r.etaMin} min", color = TextGray, fontSize = 13.sp)
            }
            Icon(Icons.Default.Star, null, tint = Amber, modifier = Modifier.size(18.dp))
            Text(" ${r.rating}")
        }
    }
}

@Composable
fun RestaurantScreen(r: Restaurant, vm: AppViewModel, onBack: () -> Unit, onCart: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(PrimaryBlue.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center) { Text(r.name, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
                Text("${r.cuisine} • ⭐ ${r.rating} • ${r.etaMin} min", color = TextGray)
            }
            r.menu.groupBy { it.category }.forEach { (cat, foods) ->
                item { Text(cat, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
                items(foods) { f ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(f.name, fontWeight = FontWeight.SemiBold)
                                Text(f.desc, color = TextGray, fontSize = 12.sp)
                                Text("₹${f.price}", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = { vm.add(r, f) }, shape = RoundedCornerShape(10.dp)) { Text("Add") }
                        }
                    }
                }
            }
        }
        if (vm.cart.isNotEmpty() && vm.cartRestaurant?.id == r.id) {
            Box(Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                PrimaryButton("View cart • ₹${vm.total}", onClick = onCart)
            }
        }
    }
}

@Composable
fun QtyButtons(qty: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMinus) { Icon(Icons.Default.Clear, "Less") }
        Text("$qty", fontWeight = FontWeight.Bold)
        IconButton(onClick = onPlus) { Icon(Icons.Default.Add, "More") }
    }
}

@Composable
fun PriceSummary(vm: AppViewModel) {
    Column {
        Row { Text("Subtotal", Modifier.weight(1f), color = TextGray); Text("₹${vm.subtotal}") }
        Row { Text("Delivery fee", Modifier.weight(1f), color = TextGray); Text("₹${vm.deliveryFee}") }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Row { Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold); Text("₹${vm.total}", fontWeight = FontWeight.Bold, color = PrimaryBlue) }
    }
}

@Composable
fun CartScreen(vm: AppViewModel, onCheckout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your cart", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        vm.cartRestaurant?.let { Text("from ${it.name}", color = TextGray) }
        if (vm.cart.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty 🛒", color = TextGray)
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(vm.cart.toList()) { line ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(line.food.name, fontWeight = FontWeight.SemiBold)
                            Text("₹${line.food.price * line.qty}", color = PrimaryBlue)
                        }
                        QtyButtons(line.qty, { vm.change(line.food.id, -1) }, { vm.change(line.food.id, 1) })
                    }
                }
            }
            PriceSummary(vm)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Checkout", onClick = onCheckout)
        }
    }
}

@Composable
fun CheckoutScreen(vm: AppViewModel, onBack: () -> Unit, onPlaced: (Order) -> Unit) {
    var address by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("UPI") }
    var paying by remember { mutableStateOf(false) }
    LaunchedEffect(paying) {
        if (paying) { delay(1500); onPlaced(vm.placeOrder(address, method)) }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
        Text("Checkout", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(address, { address = it }, label = { Text("Delivery address") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Text("Payment method (mock)", fontWeight = FontWeight.SemiBold)
        listOf("UPI", "Card", "Cash on delivery").forEach { m ->
            Row(Modifier.fillMaxWidth().clickable { method = m }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = method == m, onClick = { method = m }); Text(m)
            }
        }
        Spacer(Modifier.weight(1f))
        PriceSummary(vm)
        Spacer(Modifier.height(12.dp))
        PrimaryButton(if (paying) "Processing payment…" else "Pay ₹${vm.total} & place order",
            enabled = !paying && address.isNotBlank() && vm.cart.isNotEmpty()) { paying = true }
    }
}

@Composable
fun TrackingScreen(o: Order, onBack: () -> Unit, onRate: () -> Unit) {
    val r = o.restaurant
    val courier = when (o.status) {
        Status.PICKED_UP -> Offset(r.x, r.y)
        Status.ON_THE_WAY, Status.DELIVERED -> Offset(
            r.x + (customerPos.x - r.x) * o.progress, r.y + (customerPos.y - r.y) * o.progress)
        else -> null
    }
    val remainingMin = if (o.status == Status.ON_THE_WAY) ((1f - o.progress) * 10).toInt() + 1 else null
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Order ${o.id}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${r.name} • ₹${o.total} • ${o.payment}", color = TextGray)
        }
        item { MapView(Modifier.fillMaxWidth().height(260.dp), listOf(r), courier, routeFrom = r) }
        item { remainingMin?.let { Text("Arriving in about $it min", color = Amber, fontWeight = FontWeight.SemiBold) } }
        items(Status.values().toList()) { s ->
            val done = s.ordinal <= o.status.ordinal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(18.dp).clip(CircleShape).background(if (done) SuccessGreen else CardBg))
                Spacer(Modifier.width(12.dp))
                Text(s.label, color = if (done) Color.White else TextGray,
                    fontWeight = if (s == o.status) FontWeight.Bold else FontWeight.Normal)
            }
        }
        if (o.status == Status.DELIVERED) item {
            Spacer(Modifier.height(8.dp)); PrimaryButton("Rate this order", onClick = onRate)
        }
    }
}

@Composable
fun OrdersScreen(vm: AppViewModel, onTrack: (Order) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Your orders", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        if (vm.orders.isEmpty()) item { Text("No orders yet", color = TextGray) }
        items(vm.orders.toList()) { o ->
            Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text("${o.id} • ${o.restaurant.name}", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text("₹${o.total}", color = PrimaryBlue)
                    }
                    Text(o.lines.joinToString { "${it.qty}× ${it.food.name}" }, color = TextGray, fontSize = 13.sp)
                    Text(o.status.label, color = if (o.status == Status.DELIVERED) SuccessGreen else Amber,
                        modifier = Modifier.padding(vertical = 4.dp))
                    if (o.status != Status.DELIVERED) {
                        OutlinedButton(onClick = { onTrack(o) }) { Text("Track order") }
                    } else {
                        Row {
                            (1..5).forEach { s ->
                                IconButton(onClick = { o.rating = s }) {
                                    Icon(Icons.Default.Star, "$s stars", tint = if (s <= o.rating) Amber else TextGray)
                                }
                            }
                        }
                        OutlinedTextField(o.review, { o.review = it }, placeholder = { Text("Write a review (optional)") },
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(vm: AppViewModel, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Profile", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(vm.userName ?: "", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(vm.userEmail, color = TextGray)
        Text("${vm.orders.size} orders placed", color = TextGray, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Log out", color = ErrorRed) }
    }
}
