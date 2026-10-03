package com.navimeal

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Food(val id: Int, val name: String, val desc: String, val price: Int, val category: String)

data class Restaurant(
    val id: Int, val name: String, val cuisine: String, val rating: Double,
    val etaMin: Int, val x: Float, val y: Float, val menu: List<Food>
)

data class CartLine(val food: Food, val qty: Int)

enum class Status(val label: String) {
    PLACED("Order placed"), CONFIRMED("Restaurant confirmed"), PREPARING("Preparing your food"),
    READY("Ready for pickup"), PICKED_UP("Picked up by partner"), ON_THE_WAY("On the way"),
    DELIVERED("Delivered")
}

class Order(
    val id: String, val restaurant: Restaurant, val lines: List<CartLine>,
    val total: Int, val payment: String, val address: String
) {
    var status by mutableStateOf(Status.PLACED)
    var progress by mutableFloatStateOf(0f)
    var rating by mutableIntStateOf(0)
    var review by mutableStateOf("")
}

private fun menu(base: Int, vararg items: Triple<String, String, Int>): List<Food> =
    items.mapIndexed { i, t -> Food(base + i, t.first, t.second, t.third, if (i < 2) "Popular" else "Mains") }

val restaurants = listOf(
    Restaurant(1, "Spice Route", "South Indian", 4.6, 25, 0.22f, 0.25f, menu(100,
        Triple("Masala Dosa", "Crisp crepe with potato filling", 120),
        Triple("Idli Sambar", "Steamed rice cakes, 3 pcs", 80),
        Triple("Chettinad Chicken", "Spicy pepper chicken curry", 260),
        Triple("Filter Coffee", "Traditional decoction", 40))),
    Restaurant(2, "Biryani House", "Biryani", 4.4, 30, 0.7f, 0.22f, menu(200,
        Triple("Chicken Biryani", "Dum-style with raita", 240),
        Triple("Mutton Biryani", "Slow cooked, rich masala", 320),
        Triple("Paneer 65", "Spicy fried paneer", 190),
        Triple("Gulab Jamun", "2 pcs", 70))),
    Restaurant(3, "Pizza Corner", "Italian", 4.2, 35, 0.85f, 0.55f, menu(300,
        Triple("Margherita", "Classic cheese pizza", 220),
        Triple("Farmhouse", "Loaded veggie pizza", 310),
        Triple("Garlic Bread", "With cheese dip", 120),
        Triple("Pasta Alfredo", "Creamy white sauce", 230))),
    Restaurant(4, "Burger Hub", "Fast Food", 4.1, 20, 0.12f, 0.6f, menu(400,
        Triple("Classic Burger", "Beef-free patty, lettuce, cheese", 160),
        Triple("Crispy Chicken Burger", "Fried chicken, mayo", 190),
        Triple("French Fries", "Salted, large", 90),
        Triple("Cold Coffee", "Iced and creamy", 110))),
    Restaurant(5, "Wok Express", "Chinese", 4.3, 28, 0.45f, 0.4f, menu(500,
        Triple("Veg Hakka Noodles", "Wok tossed", 150),
        Triple("Chicken Fried Rice", "With egg", 190),
        Triple("Gobi Manchurian", "Dry or gravy", 160),
        Triple("Spring Rolls", "6 pcs", 130))),
    Restaurant(6, "Sweet Tooth", "Desserts", 4.7, 18, 0.62f, 0.7f, menu(600,
        Triple("Brownie Sundae", "Warm brownie, ice cream", 180),
        Triple("Red Velvet Slice", "Cream cheese frosting", 150),
        Triple("Falooda", "Rose, vermicelli, ice cream", 140),
        Triple("Choco Shake", "Thick and chocolatey", 130)))
)

class AppViewModel : ViewModel() {
    var userName by mutableStateOf<String?>(null)
    var userEmail by mutableStateOf("")
    val cart = mutableStateListOf<CartLine>()
    var cartRestaurant by mutableStateOf<Restaurant?>(null)
    val orders = mutableStateListOf<Order>()
    val deliveryFee = 30

    val subtotal: Int get() = cart.sumOf { it.food.price * it.qty }
    val total: Int get() = if (cart.isEmpty()) 0 else subtotal + deliveryFee

    fun login(name: String, email: String) { userName = name; userEmail = email }
    fun logout() { userName = null; userEmail = ""; cart.clear(); cartRestaurant = null }

    fun add(r: Restaurant, f: Food) {
        if (cartRestaurant?.id != r.id) { cart.clear(); cartRestaurant = r }
        val i = cart.indexOfFirst { it.food.id == f.id }
        if (i >= 0) cart[i] = cart[i].copy(qty = cart[i].qty + 1) else cart.add(CartLine(f, 1))
    }

    fun change(foodId: Int, delta: Int) {
        val i = cart.indexOfFirst { it.food.id == foodId }
        if (i < 0) return
        val q = cart[i].qty + delta
        if (q <= 0) cart.removeAt(i) else cart[i] = cart[i].copy(qty = q)
        if (cart.isEmpty()) cartRestaurant = null
    }

    fun placeOrder(address: String, payment: String): Order {
        val order = Order("NM" + (1000 + orders.size + 1), cartRestaurant!!, cart.toList(), total, payment, address)
        orders.add(0, order)
        cart.clear(); cartRestaurant = null
        simulate(order)
        return order
    }

    // Simulated delivery: each stage advances on a timer; courier moves along the route.
    private fun simulate(o: Order) = viewModelScope.launch {
        delay(2000); o.status = Status.CONFIRMED
        delay(4000); o.status = Status.PREPARING
        delay(6000); o.status = Status.READY
        delay(3000); o.status = Status.PICKED_UP
        delay(2000); o.status = Status.ON_THE_WAY
        val steps = 60
        for (i in 1..steps) { delay(500); o.progress = i / steps.toFloat() }
        o.status = Status.DELIVERED
    }
}
