package routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.OrderItemsTable
import persistence.OrdersTable

fun Application.registerOrderRoutes() {

    routing {
        listOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
        newOrderRoute()
    }

}

fun Route.listOrdersRoute() {

    get("/order") {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }

}

fun Route.newOrderRoute(){

    post("/order"){

        val orderRetrived = call.receive<Order>()

        transaction {

            val orderID = OrdersTable.insert {
            } get OrdersTable.id

            var items = mutableListOf<OrderItem>()

            for (itemOrder in orderRetrived.contents) {
                val itemID = OrderItemsTable.insert {
                    it[amount] = itemOrder.amount
                    it[price] = itemOrder.price.toBigDecimal()
                    it[item] = itemOrder.item
                    it[order] = orderID.value
                }get OrderItemsTable.id

                items.add(OrderItem(itemID.toString(),itemOrder.amount,itemOrder.price))
            }

            orderStorage.add(Order(orderID.toString(), items))

        }

        call.respondText("Customer stored correctly", status = HttpStatusCode.Accepted)
    }
}

fun Route.getOrderRoute() {

    get("/order/{id}") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)

        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )

        call.respond(order)
    }

}

fun Route.totalizeOrderRoute() {

    get("/order/{id}/total") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)

        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.map { it.price * it.amount }.sum()

        call.respond(total)
    }

}