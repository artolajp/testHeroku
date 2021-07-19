package routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.Customer
import models.customerStorage
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.CustomersTable

fun Application.registerCustomerRoutes() {
    routing {
        customerRouting()
    }
}

fun Route.customerRouting() {

    route("/customer") {

        get {
            if (customerStorage.isNotEmpty()) {
                call.respond(customerStorage)
            } else {
                call.respondText("No customers found", status = HttpStatusCode.NotFound)
            }
        }


        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )

            val customer =
                customerStorage.find { it.id == id.toInt() } ?: return@get call.respondText(
                    "No customer with id $id",
                    status = HttpStatusCode.NotFound
                )

            call.respond(customer)
        }


        post {
            val customer = call.receive<Customer>()

            transaction {
                val customerID = CustomersTable.insert {
                    it[firstName] = customer.firstName
                    it[lastName] = customer.lastName
                    it[email] = customer.email
                } get CustomersTable.id

                customerStorage.add(Customer(customerID.value, customer.firstName, customer.lastName, customer.email))

            }

            call.respondText("Customer stored correctly", status = HttpStatusCode.Accepted)
        }


        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            if (customerStorage.removeIf { it.id == id.toInt() }) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
                transaction {
                    CustomersTable.deleteWhere { CustomersTable.id eq id.toInt() }
                }
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

    }

}