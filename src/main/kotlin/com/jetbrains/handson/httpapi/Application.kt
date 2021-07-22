package com.jetbrains.handson.httpapi

import com.jetbrains.handson.chat.server.Connection
import io.ktor.application.Application
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import models.customerStorage
import models.orderStorage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.*
import routes.registerCustomerRoutes
import routes.registerOrderRoutes
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)



fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    registerCustomerRoutes()
    registerOrderRoutes()

    routing {
        get("/") {
            call.respondText("Kotlin Api test with ktor and exposed!", contentType = ContentType.Text.Plain)
        }
    }

    val db = Database.connect(
        url = "jdbc:postgresql://ec2-52-86-2-228.compute-1.amazonaws.com:5432/d20763mm8c976l",
        driver = "org.postgresql.Driver",
        user = "ebqglcuhkpvenx",
        password = "dccc74a45704de91919e5b3b68ce61db6b08e7e1939b283a47075145ee8f0e3a"
    )

    transaction {
        //addLogger(StdOutSqlLogger)

        SchemaUtils.create (CustomersTable,OrderItemsTable,OrdersTable)

        customerStorage.addAll(CustomerEntity.all().map { it.toCustomer()})
        orderStorage.addAll(OrderEntity.all().map { it.toOrder()})
    }

    //websocket

    install(WebSockets)

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }


}


